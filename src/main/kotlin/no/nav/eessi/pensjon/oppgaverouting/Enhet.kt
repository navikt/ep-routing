package no.nav.eessi.pensjon.oppgaverouting

import com.fasterxml.jackson.annotation.JsonValue

enum class Enhet( @JsonValue val enhetsNr: String ) {
    FAMILIE_OG_PENSJONSYTELSER_AALESUND("4815"),
    FAMILIE_OG_PENSJONSYTELSER_OSLO("4803"),
    FAMILIE_OG_PENSJONSYTELSER_PORSGRUNN("4808"),

    ARBEID_OG_YTELSER_SORLANDET("4410"),
    ARBEID_OG_YTELSER_TONSBERG("4407"),
    ARBEID_OG_YTELSER_INNLANDET("4405"),
    ARBEID_OG_YTELSER_MORE_OG_ROMSDAL("4415"),
    ARBEID_OG_YTELSER_TRONDHEIM("4416"),
    ARBEID_OG_YTELSER_ROMERIKE("4402"),
    ARBEID_OG_YTELSER_KRISTIANIA("4403"),
    ARBEID_OG_YTELSER_KARMOY("4411"),

    PENSJON_UTLAND("0001"),
    UFORE_UTLANDSTILSNITT("4476"),
    UFORE_UTLAND("4475"),
    NFP_UTLAND_AALESUND("4862"),
    ID_OG_FORDELING("4303"),
    DISKRESJONSKODE("2103"), //Vikafossen strengt fortrolig SPSF
    OKONOMI_PENSJON("4819"),
    //AUTOMATISK_JOURNALFORING("9999"),
    UGYLDIG_ARKIV_TYPE(""); //må være blank for oppgave støtter ikke enhetnr 9999.

    companion object {
        fun getEnhet(enhetsNr: String): Enhet? = entries.find { it.enhetsNr == enhetsNr }
    }
}