package no.nav.eessi.pensjon.oppgaverouting

import io.mockk.every
import io.mockk.mockk
import no.nav.eessi.pensjon.eux.model.BucType.*
import no.nav.eessi.pensjon.eux.model.buc.SakType


import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource

internal class Pbuc04Test {

    private val handler = EnhetFactory.hentHandlerFor(P_BUC_04) as Pbuc04

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

    @ParameterizedTest
    @EnumSource(SakType::class)
    fun `Enhet skal være NFP_UTLAND_AALESUND uavhengig av saktype`(type: SakType) {
        val request = mockk<OppgaveRoutingRequest> {
            every { hendelseType } returns HendelseType.SENDT
            every { harAdressebeskyttelse } returns false
            every { saktype } returns type
            every { aktorId } returns "111"
            every { sakInformasjon?.sakId } returns "555"
            every { sedType } returns null
            every { bucType } returns P_BUC_04
            every { bosatt } returns Bosatt.NORGE
        }

        assertEquals(Enhet.NFP_UTLAND_AALESUND, handler.finnEnhet(request))
    }

    @ParameterizedTest
    @EnumSource(SakType::class)
    fun `Mottatt hendelse skal aldri journalføres automatisk`(type: SakType) {
        val request = mockk<OppgaveRoutingRequest> {
            every { hendelseType } returns HendelseType.MOTTATT
            every { harAdressebeskyttelse } returns false
            every { saktype } returns type
            every { aktorId } returns "111"
            every { sakInformasjon?.sakId } returns "555"
            every { bosatt } returns Bosatt.NORGE
            every { sedType } returns null
            every { bucType } returns P_BUC_04
        }

        assertNotEquals(Enhet.ID_OG_FORDELING, handler.finnEnhet(request))
    }

    @ParameterizedTest
    @EnumSource(HendelseType::class)
    fun `Manuell behandling, bosatt norge`(hendelse: HendelseType) {
        val request = mockk<OppgaveRoutingRequest> {
            every { hendelseType } returns hendelse
            every { harAdressebeskyttelse } returns false
            every { saktype } returns null
            every { aktorId } returns null
            every { sakInformasjon } returns null
            every { bosatt } returns Bosatt.NORGE
            every { sedType } returns null
            every { bucType } returns P_BUC_04
        }

        assertEquals(Enhet.NFP_UTLAND_AALESUND, handler.finnEnhet(request))
    }

    @ParameterizedTest
    @EnumSource(HendelseType::class)
    fun `Manuell behandling, bosatt utland`(hendelse: HendelseType) {
        val request = mockk<OppgaveRoutingRequest> {
            every { hendelseType } returns hendelse
            every { harAdressebeskyttelse } returns false
            every { saktype } returns null
            every { aktorId } returns null
            every { sakInformasjon } returns null
            every { bosatt } returns Bosatt.UTLAND
            every { sedType } returns null
            every { bucType } returns P_BUC_04
        }

        assertEquals(Enhet.PENSJON_UTLAND, handler.finnEnhet(request))
    }
}
