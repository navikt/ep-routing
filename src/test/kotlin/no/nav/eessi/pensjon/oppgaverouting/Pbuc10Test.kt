package no.nav.eessi.pensjon.oppgaverouting

import io.mockk.every
import io.mockk.mockk
import no.nav.eessi.pensjon.eux.model.BucType.P_BUC_10
import no.nav.eessi.pensjon.eux.model.SedType
import no.nav.eessi.pensjon.eux.model.buc.SakType
import no.nav.eessi.pensjon.eux.model.buc.SakType.*
import no.nav.eessi.pensjon.oppgaverouting.HendelseType.MOTTATT
import no.nav.eessi.pensjon.oppgaverouting.HendelseType.SENDT
import no.nav.eessi.pensjon.personoppslag.pdl.model.IdentifisertPerson
import no.nav.eessi.pensjon.personoppslag.pdl.model.Relasjon
import no.nav.eessi.pensjon.personoppslag.pdl.model.SEDPersonRelasjon
import no.nav.eessi.pensjon.shared.person.Fodselsnummer
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource


internal class Pbuc10Test {

    companion object {
        private val DUMMY_FNR = Fodselsnummer.fra("09035225916") // Testbruker SLAPP SKILPADDE
    }

    private val handler = EnhetFactory.hentHandlerFor(P_BUC_10) as Pbuc10

    class IdentifisertPersonTest(
        override val aktoerId: String,
        override val fnr: Fodselsnummer?,
        override val geografiskTilknytning: String?,
        override val landkode: String?,
        override val personListe: List<IdentifisertPerson>?,
        override val personRelasjon: SEDPersonRelasjon?
    ) : IdentifisertPerson

    @Test
    fun `Inneholder diskresjonskode`() {
        val request = mockk<OppgaveRoutingRequest>(relaxed = true)

        // SPSF er strengt fortrolig og skal returnere Enhet.DISKRESJONSKODE (vikafossen)
        every { request.harAdressebeskyttelse } returns true
        assertEquals(Enhet.DISKRESJONSKODE, handler.finnEnhet(request))

        // SPSF er mindre fortrolig og følger vanlig saksflyt
        every { request.harAdressebeskyttelse } returns false
        assertNotEquals(Enhet.DISKRESJONSKODE, handler.finnEnhet(request))
    }

    @Test
    fun `Kan ikke automatisk journalføres`() {
        val request = mockk<OppgaveRoutingRequest>(relaxed = true) {
            every { hendelseType } returns SENDT
            every { saktype } returns UFOREP
            every { aktorId } returns "111"
            every { sakInformasjon?.sakId } returns "555"
        }

        assertEquals(Enhet.UFORE_UTLAND, handler.finnEnhet(request))
    }

    @Test
    fun `Mottatt sak til manuell behandling, bosatt norge`() {
        assertEquals(
                Enhet.NFP_UTLAND_AALESUND,
                handler.finnEnhet(manuellRequest(MOTTATT, ALDER, Bosatt.NORGE))
        )
        assertEquals(
                Enhet.NFP_UTLAND_AALESUND,
                handler.finnEnhet(manuellRequest(MOTTATT, GJENLEV, Bosatt.NORGE))
        )
        assertEquals(
                Enhet.UFORE_UTLANDSTILSNITT,
                handler.finnEnhet(manuellRequest(MOTTATT, UFOREP, Bosatt.NORGE))
        )
        assertEquals(
                Enhet.ID_OG_FORDELING,
                handler.finnEnhet(manuellRequest(MOTTATT, OMSORG, Bosatt.NORGE))
        )
        assertEquals(
                Enhet.ID_OG_FORDELING,
                handler.finnEnhet(manuellRequest(MOTTATT, GENRL, Bosatt.NORGE))
        )
        assertEquals(
                Enhet.ID_OG_FORDELING,
                handler.finnEnhet(manuellRequest(MOTTATT, BARNEP, Bosatt.NORGE))
        )
    }

    @Test
    fun `Sendt sak til manuell behandling, bosatt norge`() {
        assertEquals(
                Enhet.ID_OG_FORDELING,
                handler.finnEnhet(manuellRequest(SENDT, ALDER, Bosatt.NORGE))
        )
        assertEquals(
                Enhet.ID_OG_FORDELING,
                handler.finnEnhet(manuellRequest(SENDT, GJENLEV, Bosatt.NORGE))
        )
        assertEquals(
                Enhet.UFORE_UTLANDSTILSNITT,
                handler.finnEnhet(manuellRequest(SENDT, UFOREP, Bosatt.NORGE))
        )
        assertEquals(
                Enhet.ID_OG_FORDELING,
                handler.finnEnhet(manuellRequest(SENDT, OMSORG, Bosatt.NORGE))
        )
        assertEquals(
                Enhet.ID_OG_FORDELING,
                handler.finnEnhet(manuellRequest(SENDT, GENRL, Bosatt.NORGE))
        )
        assertEquals(
                Enhet.ID_OG_FORDELING,
                handler.finnEnhet(manuellRequest(SENDT, BARNEP, Bosatt.NORGE))
        )
    }

    @ParameterizedTest
    @EnumSource(HendelseType::class)
    fun `Mottatt sak til manuell behandling, bosatt utland`(hendelse: HendelseType) {
        assertEquals(
                Enhet.PENSJON_UTLAND,
                handler.finnEnhet(manuellRequest(hendelse, ALDER, Bosatt.UTLAND))
        )
        assertEquals(
                Enhet.PENSJON_UTLAND,
                handler.finnEnhet(manuellRequest(hendelse, GJENLEV, Bosatt.UTLAND))
        )
        assertEquals(
                Enhet.UFORE_UTLAND,
                handler.finnEnhet(manuellRequest(hendelse, UFOREP, Bosatt.UTLAND))
        )
        assertEquals(
                Enhet.PENSJON_UTLAND,
                handler.finnEnhet(manuellRequest(hendelse, OMSORG, Bosatt.UTLAND))
        )
        assertEquals(
                Enhet.PENSJON_UTLAND,
                handler.finnEnhet(manuellRequest(hendelse, GENRL, Bosatt.UTLAND))
        )
        assertEquals(
                Enhet.PENSJON_UTLAND,
                handler.finnEnhet(manuellRequest(hendelse, BARNEP, Bosatt.UTLAND))
        )
    }

    private fun manuellRequest(
        hendelse: HendelseType,
        ytelse: SakType?,
        land: Bosatt
    ): OppgaveRoutingRequest {
        return mockk {
            if (hendelse == SENDT)
                every { identifisertPerson } returns IdentifisertPersonTest(
                    aktoerId = "1231", fnr = DUMMY_FNR, landkode = "NOR", geografiskTilknytning = "1234", personListe = null, personRelasjon = SEDPersonRelasjon(
                        DUMMY_FNR, Relasjon.GJENLEVENDE, GJENLEV, SedType.P15000, rinaDocumentId =  "3123123"
                    )
                )
            every { harAdressebeskyttelse } returns false
            every { hendelseType } returns hendelse
            every { saktype } returns ytelse
            every { aktorId } returns "111"
            every { landkode } returns "NOR"
            every { bosatt } returns land
            every { sakInformasjon } returns null
            every { bucType } returns P_BUC_10
            every { sedType } returns SedType.P15000


        }
    }
}
