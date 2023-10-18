package no.nav.eessi.pensjon.oppgaverouting

import no.nav.eessi.pensjon.eux.model.buc.SakType.*
import no.nav.eessi.pensjon.personoppslag.pdl.model.IdentifisertPerson
import no.nav.eessi.pensjon.personoppslag.pdl.model.Relasjon

class Pbuc05 : EnhetHandler {
    override fun finnEnhet(request: OppgaveRoutingRequest): Enhet {
        return if (request.hendelseType == HendelseType.SENDT) enhetForSendt(request)
        else enhetForMottatt(request)
    }

    private fun enhetForSendt(request: OppgaveRoutingRequest): Enhet {
        return when {
            request.sakInformasjon == null -> {
                logger.info(" ${request.sedType} i ${request.bucType} gir enhet ${Enhet.ID_OG_FORDELING.enhetsNr} på grunn av manglende saksinformasjon")
                Enhet.ID_OG_FORDELING
            }
            erGjenlevende(request.identifisertPerson) -> enhetFraAlderOgLand(request)
            harRelasjoner(request.identifisertPerson) -> hentEnhetForRelasjon(request)
            else -> enhetFraAlderOgLand(request)
        }
    }

    private fun enhetForMottatt(request: OppgaveRoutingRequest): Enhet {
        val personListe = request.identifisertPerson?.personListe ?: emptyList()

        if (request.identifisertPerson?.personRelasjon?.fnr == null) {
            logger.info("Mottatt ${request.sedType} i ${request.bucType} gir enhet ${Enhet.ID_OG_FORDELING.enhetsNr} på grunn av manglende fødselsnummer")
            return Enhet.ID_OG_FORDELING
        }

        return if (personListe.isEmpty()) {
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
                personListe.any { it.personRelasjon?.relasjon == Relasjon.FORSORGER } -> enhetFraAlderOgLand(request)
                personListe.any { it.personRelasjon?.relasjon == Relasjon.BARN } -> enhetFraAlderOgLand(request)
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
            person?.personRelasjon?.relasjon == Relasjon.GJENLEVENDE

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
            personer.any { it.personRelasjon?.relasjon == Relasjon.BARN } -> enhetForRelasjonBarn(request)
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

        return if (request.bosatt == Bosatt.NORGE) {
            logger.info("${request.sedType} i ${request.bucType} gir enhet ${Enhet.NFP_UTLAND_AALESUND.enhetsNr} på grunn av bosatt norge med personrelasjon: Barn ")
            Enhet.NFP_UTLAND_AALESUND
        } else {
            logger.info("${request.sedType} i ${request.bucType} gir enhet ${Enhet.PENSJON_UTLAND.enhetsNr} på grunn av bosatt utland med personrelasjon: Barn ")
            Enhet.PENSJON_UTLAND
        }
    }
}
