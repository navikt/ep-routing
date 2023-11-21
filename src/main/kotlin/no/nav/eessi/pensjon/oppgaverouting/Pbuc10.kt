package no.nav.eessi.pensjon.oppgaverouting

import no.nav.eessi.pensjon.eux.model.buc.SakType.*
import no.nav.eessi.pensjon.oppgaverouting.Enhet.*


class Pbuc10 : EnhetHandler {

    override fun finnEnhet(request: OppgaveRoutingRequest): Enhet {
        return when {
            request.harAdressebeskyttelse -> {
                adresseBeskyttelseLogging(request.sedType, request.bucType, DISKRESJONSKODE)
                DISKRESJONSKODE
            }
            request.bosatt == Bosatt.NORGE && (request.hendelseType == HendelseType.MOTTATT && (request.saktype == ALDER || request.saktype == GJENLEV)) -> {
                logger.info("${request.hendelseType} ${request.sedType} i ${request.bucType} gir enhet $NFP_UTLAND_AALESUND på grunn av bosatt Norge, alder eller gjenlevende-sak")
                NFP_UTLAND_AALESUND
            }
            request.bosatt == Bosatt.NORGE && request.saktype == UFOREP -> {
                logger.info("${request.hendelseType} ${request.sedType} i ${request.bucType} gir enhet $UFORE_UTLANDSTILSNITT på grunn av, bosatt Norge, uføre-sak")
                UFORE_UTLANDSTILSNITT
            }
            request.bosatt == Bosatt.NORGE && request.saktype != UFOREP -> {
                logger.info("${request.hendelseType} ${request.sedType} i ${request.bucType} gir enhet $ID_OG_FORDELING på grunn av sak er hverken alder, gjenlevende eller uføre")
                ID_OG_FORDELING
            }
            request.saktype == UFOREP -> {
                logger.info("${request.hendelseType} ${request.sedType} i ${request.bucType} gir enhet $UFORE_UTLAND på grunn av bosatt utland og sak er uføre")
                UFORE_UTLAND
            }
            else -> {
                logger.info("${request.hendelseType} ${request.sedType} i ${request.bucType} gir enhet $PENSJON_UTLAND på grunn av bosatt utland og sak er ikke uføre")
                PENSJON_UTLAND
            }
        }
    }

}
