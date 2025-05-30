package no.nav.eessi.pensjon.oppgaverouting

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import no.nav.eessi.pensjon.eux.model.buc.SakStatus
import no.nav.eessi.pensjon.eux.model.buc.SakType

@JsonIgnoreProperties(ignoreUnknown = true)
data class SakInformasjon(
    val sakId: String?,
    val sakType: SakType,
    val sakStatus: SakStatus,
    val saksbehandlendeEnhetId: String = "",
    val nyopprettet: Boolean = false,
)