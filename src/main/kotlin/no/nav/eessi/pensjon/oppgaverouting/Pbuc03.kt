package no.nav.eessi.pensjon.oppgaverouting



class Pbuc03 : EnhetHandler {

    override fun finnEnhet(request: OppgaveRoutingRequest): Enhet {
        return when {
            request.harAdressebeskyttelse -> {
                adresseBeskyttelseLogging(request.sedType, request.bucType, Enhet.DISKRESJONSKODE)
                Enhet.DISKRESJONSKODE
            }
            kanAutomatiskJournalfores(request) ->  {
                automatiskJournalforingLogging(request.sedType, request.bucType, Enhet.AUTOMATISK_JOURNALFORING)
                Enhet.AUTOMATISK_JOURNALFORING
            }
            request.bosatt == Bosatt.NORGE ->  {
                bosattNorgeLogging(request.sedType, request.bucType, Enhet.UFORE_UTLANDSTILSNITT)
                Enhet.UFORE_UTLANDSTILSNITT
            }
            else -> {
                ingenSĂ¦rreglerLogging(request.sedType, request.bucType, Enhet.UFORE_UTLANDSTILSNITT)
                Enhet.UFORE_UTLAND
            }
        }
    }

    override fun kanAutomatiskJournalfores(request: OppgaveRoutingRequest): Boolean {
        return request.run {
            saktype != null
                    && !aktorId.isNullOrBlank()
                    && !sakInformasjon?.sakId.isNullOrBlank()
        }
    }
}
