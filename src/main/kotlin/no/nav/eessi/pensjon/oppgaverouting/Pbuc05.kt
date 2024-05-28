package no.nav.eessi.pensjon.oppgaverouting

import no.nav.eessi.pensjon.eux.model.buc.SakType
import no.nav.eessi.pensjon.personoppslag.pdl.model.IdentifisertPerson
import no.nav.eessi.pensjon.personoppslag.pdl.model.Relasjon
import no.nav.eessi.pensjon.personoppslag.pdl.model.Relasjon.*

class Pbuc05 : EnhetHandler {
    override fun finnEnhet(request: OppgaveRoutingRequest): Enhet {
        return if (request.hendelseType == HendelseType.SENDT) enhetForSendt(request)
        else enhetForMottatt(request)
    }

    private fun enhetForSendt(request: OppgaveRoutingRequest): Enhet {
        return when {
            request.sakInformasjon == null -> {
                logger.info("${request.sedType} i ${request.bucType} gir enhet ${Enhet.ID_OG_FORDELING.enhetsNr} på grunn av manglende saksinformasjon")
                Enhet.ID_OG_FORDELING
            }
            erGjenlevende(request.identifisertPerson) -> enhetFraAlderOgLand(request)
            harRelasjoner(request.identifisertPerson) -> hentEnhetForRelasjon(request)
            else -> enhetFraAlderOgLand(request)
        }
    }

    private fun enhetForMottatt(request: OppgaveRoutingRequest): Enhet {
        val personRelasjonerListe = request.identifisertPerson?.personListe ?: emptyList()

        if (request.identifisertPerson?.personRelasjon?.fnr == null) {
            logger.info("Mottatt ${request.sedType} i ${request.bucType} gir enhet ${Enhet.ID_OG_FORDELING.enhetsNr} på grunn av manglende fødselsnummer")
            return Enhet.ID_OG_FORDELING
        }

        return if (personRelasjonerListe.isEmpty()) {
            if (erGjenlevende(request.identifisertPerson)) {
                if (request.bosatt == Bosatt.NORGE) {
                    logger.info("Mottatt ${request.sedType} i ${request.bucType} gir enhet ${Enhet.NFP_UTLAND_AALESUND.enhetsNr} på grunn av bosatt Norge og person er gjenlevende")
                    Enhet.NFP_UTLAND_AALESUND
                }
                else {
                    logger.info("Mottatt ${request.sedType} i ${request.bucType} gir enhet ${Enhet.PENSJON_UTLAND.enhetsNr} på grunn av bosatt Norge og person er ikke gjenlevende")
                    Enhet.PENSJON_UTLAND
                }
            } else enhetFraAlderOgLand(request)
        } else {
            when {
                personRelasjonerListe.any { it.personRelasjon?.relasjon in listOf(FORSORGER, BARN) } -> enhetFraAlderOgLand(request)
                else -> {
                    logger.info("Mottatt ${request.sedType} i ${request.bucType} gir enhet ${Enhet.ID_OG_FORDELING.enhetsNr} på grunn av det finnes personrelasjoner men disse er hverken forsørger eller barn")
                    Enhet.ID_OG_FORDELING
                }
            }
        }
    }

    /**
     * Sjekker om det finnes en identifisert person og om denne personen er [Relasjon.GJENLEVENDE]
     *
     * @return true dersom personen har [Relasjon.GJENLEVENDE]
     */
    private fun erGjenlevende(person: IdentifisertPerson?): Boolean =
            person?.personRelasjon?.relasjon == GJENLEVENDE

    /**
     * Sjekker om saken inneholder flere identifiserte personer.
     *
     * @return true dersom det finnes mer enn én person.
     */
    private fun harRelasjoner(identifisertPerson: IdentifisertPerson?): Boolean {
        return identifisertPerson?.personListe?.isNotEmpty() ?: false
    }

    /**
     * Henter ut enhet basert på [Boolean].
     * Skal kun brukes dersom det finnes en [IdentifisertPerson] med [Relasjon.BARN]
     *
     * @return Kaller ruting-funksjon basert på [Relasjon]
     */
    private fun hentEnhetForRelasjon(request: OppgaveRoutingRequest): Enhet {
        val personer = request.identifisertPerson?.personListe ?: return enhetFraAlderOgLand(request)

        return when {
            personer.any { it.personRelasjon?.relasjon == BARN } -> enhetForRelasjonBarn(request)
            else -> enhetFraAlderOgLand(request)
        }
    }

    /**
     * Henter korrekt enhet for [Relasjon.BARN]
     *
     * @return Skal returnere forenklet rutingregel .
     */
    private fun enhetForRelasjonBarn(request: OppgaveRoutingRequest): Enhet {
        if (request.sakInformasjon?.sakId == null) {
            logger.info("${request.sedType} i ${request.bucType} gir enhet ${Enhet.ID_OG_FORDELING.enhetsNr} på grunn av manglende pesys saksId")
            return Enhet.ID_OG_FORDELING
        }

        //TODO: Her kommer enheten saksbehandler er ansatt (når denne sendes fra RINA, et sted lang, langt der fremme)
        return when (request.bosatt) {
            Bosatt.NORGE -> {
                if (request.saktype == SakType.UFOREP) {
                    logger.info("${request.sedType} i ${request.bucType} gir enhet ${Enhet.UFORE_UTLANDSTILSNITT.enhetsNr} på grunn av bosatt norge med personrelasjon: Barn ")
                    Enhet.UFORE_UTLANDSTILSNITT
                } else {
                    logger.info("${request.sedType} i ${request.bucType} gir enhet ${Enhet.NFP_UTLAND_AALESUND.enhetsNr} på grunn av bosatt norge med personrelasjon: Barn ")
                    Enhet.NFP_UTLAND_AALESUND
                }
            }
            else -> {
                if (request.saktype == SakType.UFOREP) {
                    logger.info("${request.sedType} i ${request.bucType} gir enhet ${Enhet.UFORE_UTLAND.enhetsNr} på grunn av bosatt utland med personrelasjon: Barn ")
                    Enhet.UFORE_UTLAND
                } else {
                        logger.info("${request.sedType} i ${request.bucType} gir enhet ${Enhet.PENSJON_UTLAND.enhetsNr} på grunn av bosatt utland med personrelasjon: Barn ")
                        Enhet.PENSJON_UTLAND
                }
            }
        }
    }

    /**
     * Henter ut [Enhet] basert på gjeldende person sin bosetning og fødselsdato.
     * Se rutingregler her: {@see https://confluence.adeo.no/pages/viewpage.action?pageId=387092731}
     *
     * @return [Enhet] basert på rutingregler.
     */
    private fun enhetFraAlderOgLand(request: OppgaveRoutingRequest): Enhet {

        logger.info("Person er bosatt:  ${request.bosatt}")
        return if (request.bosatt == Bosatt.NORGE) {
            if (request.avsenderLand == "DE" && request.fdato.ageIsBetween18and60()) {
                logger.info("${request.sedType} i ${request.bucType} gir enhet ${Enhet.UFORE_UTLANDSTILSNITT.enhetsNr} på grunn av bosatt norge, avsenderland er DE og alder er mellom 18 og 60")
                Enhet.UFORE_UTLANDSTILSNITT
            }
            else if (request.avsenderLand != "DE" && request.fdato.ageIsBetween18and62()) {
                logger.info("${request.sedType} i ${request.bucType} gir enhet ${Enhet.UFORE_UTLANDSTILSNITT.enhetsNr} på grunn av bosatt norge, avsenderland ikke er DE og alder er mellom 18 og 62")
                Enhet.UFORE_UTLANDSTILSNITT
            }
            else {
                logger.info("${request.sedType} i ${request.bucType} gir enhet ${Enhet.NFP_UTLAND_AALESUND.enhetsNr} på grunn av bosatt norge og alder er NFP")
                Enhet.NFP_UTLAND_AALESUND
            }
        }
        else if (request.bosatt == Bosatt.UTLAND){
            if (request.fdato.ageIsBetween18and62()) {
                logger.info("${request.sedType} i ${request.bucType} gir enhet ${Enhet.UFORE_UTLAND.enhetsNr} på grunn av bosatt utland og alder er NAY")
                Enhet.UFORE_UTLAND
            }
            else {
                logger.info("${request.sedType} i ${request.bucType} gir enhet ${Enhet.PENSJON_UTLAND.enhetsNr} på grunn av bosatt utland og alder er NFP")
                Enhet.PENSJON_UTLAND
            }
        } else {
            logger.info("${request.sedType} i ${request.bucType} gir enhet ${Enhet.ID_OG_FORDELING.enhetsNr} på grunn av at bosatt er ukjent")
            Enhet.ID_OG_FORDELING
        }
    }
}
