package no.nav.eessi.pensjon.oppgaverouting



class Hbuc07 : EnhetHandler {
    override fun finnEnhet(request: OppgaveRoutingRequest): Enhet {
        return if (request.harAdressebeskyttelse)
            Enhet.DISKRESJONSKODE
        else
            enhetFraAlderOgLand(request)
    }

    private fun enhetFraAlderOgLand(request: OppgaveRoutingRequest): Enhet {
        val ageIsBetween18and60 = request.fdato.ageIsBetween18and60()

        return if (request.bosatt == Bosatt.NORGE) {
            if (ageIsBetween18and60) Enhet.UFORE_UTLANDSTILSNITT
            else Enhet.FAMILIE_OG_PENSJONSYTELSER_OSLO
        } else {
            if (ageIsBetween18and60) Enhet.UFORE_UTLAND
            else Enhet.PENSJON_UTLAND
        }
    }
}
