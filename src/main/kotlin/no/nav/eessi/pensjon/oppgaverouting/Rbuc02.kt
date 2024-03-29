package no.nav.eessi.pensjon.oppgaverouting

import no.nav.eessi.pensjon.eux.model.SedType.R004
import no.nav.eessi.pensjon.eux.model.buc.SakType.ALDER
import no.nav.eessi.pensjon.eux.model.buc.SakType.UFOREP
import no.nav.eessi.pensjon.personoppslag.pdl.model.IdentifisertPerson

/**
 * R_BUC_02: Motregning av overskytende utbetaling i etterbetalinger
 */
class Rbuc02 : EnhetHandler {
    override fun finnEnhet(request: OppgaveRoutingRequest): Enhet {
        return when {
            request.harAdressebeskyttelse -> {
                adresseBeskyttelseLogging(request.sedType, request.bucType, Enhet.DISKRESJONSKODE)
                Enhet.DISKRESJONSKODE
            }
            erPersonUgyldig(request.identifisertPerson) -> {
                logger.info("${request.sedType} i ${request.bucType} gir enhet ${Enhet.ID_OG_FORDELING.enhetsNr} på grunn av ingen treff på ${request.identifisertPerson?.personListe?.size} person(er).")
                Enhet.ID_OG_FORDELING
            }
            request.sedType == R004 -> {
                logger.info("${request.sedType} i ${request.bucType} gir enhet ${Enhet.OKONOMI_PENSJON.enhetsNr} på grunn av SED er R004")
                Enhet.OKONOMI_PENSJON
            }
            else -> hentEnhetForYtelse(request)
        }
    }

    private fun hentEnhetForYtelse(request: OppgaveRoutingRequest): Enhet {
        return if (request.hendelseType == HendelseType.SENDT) {
            logger.info("${request.sedType} i ${request.bucType} gir enhet ${Enhet.ID_OG_FORDELING.enhetsNr} på grunn av traff ingen særregler og SED er sendt")
            Enhet.ID_OG_FORDELING
        } else {
            when (request.saktype) {
                ALDER -> {
                    logger.info("${request.sedType} i ${request.bucType} gir enhet ${Enhet.PENSJON_UTLAND.enhetsNr} på grunn av traff ingen særregler og SED er mottatt med sakstype: alder")
                    Enhet.PENSJON_UTLAND
                }
                UFOREP -> {
                    logger.info("${request.sedType} i ${request.bucType} gir enhet ${Enhet.UFORE_UTLAND.enhetsNr} på grunn av traff ingen særregler og SED er mottatt med sakstype: uføre")
                    Enhet.UFORE_UTLAND
                }
                else -> {
                    logger.info("${request.sedType} i ${request.bucType} gir enhet ${Enhet.ID_OG_FORDELING.enhetsNr} på grunn av traff ingen særregler og SED er mottatt men sakstype er hverken alder eller uføre")
                    Enhet.ID_OG_FORDELING
                }
            }
        }
    }

    private fun erPersonUgyldig(person: IdentifisertPerson?): Boolean =
            person == null || person.aktoerId.isBlank() || person.flereEnnEnPerson()
}
