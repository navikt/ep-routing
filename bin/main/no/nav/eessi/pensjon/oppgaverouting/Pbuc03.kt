package no.nav.eessi.pensjon.oppgaverouting



class Pbuc03 : EnhetHandler {

    override fun finnEnhet(request: OppgaveRoutingRequest): Enhet {
        return when {
            request.harAdressebeskyttelse -> {
                adresseBeskyttelseLogging(request.sedType, request.bucType, Enhet.DISKRESJONSKODE)
                Enhet.DISKRESJONSKODE
            }
            request.bosatt == Bosatt.NORGE ->  {
                bosattNorgeLogging(request.sedType, request.bucType, Enhet.UFORE_UTLANDSTILSNITT)
                Enhet.UFORE_UTLANDSTILSNITT
            }
            else -> {
                ingenSærreglerLogging(request.sedType, request.bucType, Enhet.UFORE_UTLANDSTILSNITT)
                Enhet.UFORE_UTLAND
            }
        }
    }
}
