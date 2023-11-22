package no.nav.eessi.pensjon.oppgaverouting

import no.nav.eessi.pensjon.eux.model.buc.SakStatus.AVSLUTTET
import no.nav.eessi.pensjon.eux.model.buc.SakType.*


/**
 * P_BUC_02: Krav om etterlatteytelser
 *
 * @see <a href="https://jira.adeo.no/browse/EP-853">Jira-sak EP-853</a>
 */
class Pbuc02 : EnhetHandler {

    override fun finnEnhet(request: OppgaveRoutingRequest): Enhet {
        return when {
            request.harAdressebeskyttelse -> {
                adresseBeskyttelseLogging(request.sedType, request.bucType, Enhet.DISKRESJONSKODE)
                Enhet.DISKRESJONSKODE
            }
            /**
             *     https://confluence.adeo.no/pages/viewpage.action?pageId=544313760
             *     Denne fases ut da ID_OG_FORDELING kun skal håndtere SED med ukjente FNR/DNR

            erUforeSakAvsluttet(request) -> {
            logger.info("${request.sedType} i ${request.bucType} gir enhet ${Enhet.ID_OG_FORDELING.enhetsNr} på grunn av uføresak er avsluttet")
            Enhet.ID_OG_FORDELING

            }
             */

            request.bosatt == Bosatt.NORGE -> {
                when (request.saktype) {
                    UFOREP -> {
                        bosattNorgeLogging(request.sedType, request. bucType, request.saktype, Enhet.UFORE_UTLANDSTILSNITT)
                        Enhet.UFORE_UTLANDSTILSNITT
                    }
                    ALDER -> {
                        bosattNorgeLogging(request.sedType, request. bucType, request.saktype, Enhet.NFP_UTLAND_AALESUND)
                        Enhet.NFP_UTLAND_AALESUND
                    }
                    BARNEP,
                    GJENLEV -> {
                        bosattNorgeLogging(request.sedType, request. bucType, request.saktype, Enhet.PENSJON_UTLAND)
                        Enhet.NFP_UTLAND_AALESUND
                    }
                    else -> {
                        logger.info("${request.sedType} i ${request.bucType} gir enhet ${Enhet.ID_OG_FORDELING.enhetsNr} på grunn av bosatt norge med ugyldig saktype")
                        Enhet.ID_OG_FORDELING
                    }
                }
            }
            else ->
                when (request.saktype) {
                    UFOREP -> {
                        bosattUtlandLogging(request.sedType, request.bucType, request.saktype, Enhet.UFORE_UTLAND)
                        Enhet.UFORE_UTLAND
                    }
                    ALDER,
                    BARNEP,
                    GJENLEV -> {
                        bosattUtlandLogging(request.sedType, request. bucType, request.saktype, Enhet.PENSJON_UTLAND)
                        Enhet.PENSJON_UTLAND
                    }
                    else -> {
                        logger.info("${request.sedType} i ${request.bucType} gir enhet ${Enhet.ID_OG_FORDELING.enhetsNr} på grunn av bosatt utland med ugyldig saktype")
                        Enhet.ID_OG_FORDELING
                    }
                }
        }
    }

    private fun erUforeSakAvsluttet(request: OppgaveRoutingRequest): Boolean {
        val sakInfo = request.sakInformasjon
        val erUforepensjon = (request.saktype == UFOREP || sakInfo?.sakType == UFOREP)

        return erUforepensjon && sakInfo?.sakStatus == AVSLUTTET
    }
}
