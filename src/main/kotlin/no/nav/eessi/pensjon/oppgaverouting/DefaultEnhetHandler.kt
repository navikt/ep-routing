package no.nav.eessi.pensjon.oppgaverouting

import no.nav.eessi.pensjon.eux.model.buc.SakType

class DefaultEnhetHandler : EnhetHandler {
    override fun finnEnhet(request: OppgaveRoutingRequest): Enhet {
        return if (request.harAdressebeskyttelse) {
            adresseBeskyttelseLogging(request.sedType, request.bucType, Enhet.DISKRESJONSKODE)
            Enhet.DISKRESJONSKODE
        }
        else
            enhetFraAlderOgLand(request)
    }

    //TODO Her må vi ta hensyn til saktype og alder ved bestemming av enhet det skal sendes til
    private fun enhetFraAlderOgLand(request: OppgaveRoutingRequest): Enhet {
        val ageIsBetween18and62 = request.fdato.ageIsBetween18and62()

        return if (request.bosatt == Bosatt.NORGE) {
            if (ageIsBetween18and62 && request.saktype != SakType.GJENLEV) {
                logger.info("${request.sedType} i ${request.bucType} gir enhet ${Enhet.UFORE_UTLANDSTILSNITT} på grunn av personen er bosatt i Norge. Enhet blir NAY")
                Enhet.UFORE_UTLANDSTILSNITT
            }
            else  {
                logger.info("${request.sedType} i ${request.bucType} gir enhet ${Enhet.NFP_UTLAND_AALESUND} på grunn av personen er bosatt i Norge. Enhet blir NFP")
                Enhet.NFP_UTLAND_AALESUND
            }
        } else {
            if (ageIsBetween18and62 && request.saktype != SakType.GJENLEV) {
                logger.info("${request.sedType} i ${request.bucType} gir enhet ${Enhet.UFORE_UTLAND} på grunn av personen er bosatt i utlandet. Enhet blir NAY")
                Enhet.UFORE_UTLAND
            }
            else {
                logger.info("${request.sedType} i ${request.bucType} gir enhet ${Enhet.PENSJON_UTLAND} på grunn av personen er bosatt i utlandet. Enhet blir NFP")
                Enhet.PENSJON_UTLAND
            }
        }
    }
}
