package no.nav.eessi.pensjon.oppgaverouting



/**
 * P_BUC_01: Krav om alderspensjon
 */
class Pbuc01 : EnhetHandler {
    override fun finnEnhet(request: OppgaveRoutingRequest): Enhet {
        return when {
            request.harAdressebeskyttelse -> {
                adresseBeskyttelseLogging(request.sedType, request.bucType, Enhet.DISKRESJONSKODE)
                Enhet.DISKRESJONSKODE
            }
            request.bosatt == Bosatt.NORGE -> {
                bosattNorgeLogging(request.sedType, request.bucType, Enhet.NFP_UTLAND_AALESUND)
                Enhet.NFP_UTLAND_AALESUND
            }
            else -> {
                ingenSærreglerLogging(request.sedType, request.bucType, Enhet.PENSJON_UTLAND)
                Enhet.PENSJON_UTLAND
            }
        }
    }
}
