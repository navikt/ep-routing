package no.nav.eessi.pensjon.oppgaverouting


import no.nav.eessi.pensjon.eux.model.BucType
import no.nav.eessi.pensjon.eux.model.BucType.H_BUC_07
import no.nav.eessi.pensjon.eux.model.BucType.P_BUC_01
import no.nav.eessi.pensjon.eux.model.BucType.P_BUC_02
import no.nav.eessi.pensjon.eux.model.BucType.P_BUC_03
import no.nav.eessi.pensjon.eux.model.BucType.P_BUC_04
import no.nav.eessi.pensjon.eux.model.BucType.P_BUC_05
import no.nav.eessi.pensjon.eux.model.BucType.P_BUC_06
import no.nav.eessi.pensjon.eux.model.BucType.P_BUC_07
import no.nav.eessi.pensjon.eux.model.BucType.P_BUC_08
import no.nav.eessi.pensjon.eux.model.BucType.P_BUC_09
import no.nav.eessi.pensjon.eux.model.BucType.P_BUC_10
import no.nav.eessi.pensjon.eux.model.BucType.R_BUC_02
import no.nav.eessi.pensjon.eux.model.SedType
import no.nav.eessi.pensjon.eux.model.buc.SakType
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.LocalDate
import java.time.Period

val logger: Logger = LoggerFactory.getLogger(EnhetHandler::class.java)

interface EnhetHandler {
    fun finnEnhet(request: OppgaveRoutingRequest): Enhet

    fun kanAutomatiskJournalfores(request: OppgaveRoutingRequest): Boolean {
        return request.run {
            hendelseType == HendelseType.SENDT
                    && saktype != null
                    && !aktorId.isNullOrBlank()
                    && !sakInformasjon?.sakId.isNullOrBlank()
        }
    }

    fun adresseBeskyttelseLogging(sedType: SedType?, bucType: BucType, enhet: Enhet) {
        logger.info("$sedType i $bucType gir enhet ${enhet.enhetsNr} på grunn av adressebeskyttelse")
    }

    fun automatiskJournalforingLogging(sedType: SedType?, bucType: BucType, enhet: Enhet) {
        logger.info("$sedType i $bucType  gir enhet ${enhet.enhetsNr} på grunn av automatisk journalføring")
    }

    fun bosattNorgeLogging(sedType: SedType?, bucType: BucType, enhet: Enhet) {
        logger.info("$sedType i $bucType  gir enhet ${enhet.enhetsNr} på grunn av personen er bosatt i norge")
    }

    fun bosattNorgeLogging(sedType: SedType?, bucType: BucType, sakType: SakType, enhet: Enhet) {
        logger.info("$sedType i $bucType  gir enhet ${enhet.enhetsNr} på grunn av saktype: $sakType og personen er bosatt i norge")
    }

    fun bosattUtlandLogging(sedType: SedType?, bucType: BucType, sakType: SakType, enhet: Enhet) {
        logger.info("$sedType i $bucType  gir enhet ${enhet.enhetsNr} på grunn av saktype: $sakType og personen er bosatt i utlandet")
    }

    fun ingenSærreglerLogging(sedType: SedType?, bucType: BucType, enhet: Enhet) {
        logger.info("$sedType i $bucType  gir enhet ${enhet.enhetsNr} på grunn av ingen særregler ble inntruffet")
    }

    /**
     * Henter ut [Enhet] basert på gjeldende person sin bosetning og fødselsdato.
     * Se rutingregler her: {@see https://confluence.adeo.no/pages/viewpage.action?pageId=387092731}
     *
     * @return [Enhet] basert på rutingregler.
     */
    fun enhetFraAlderOgLand(request: OppgaveRoutingRequest): Enhet {

        logger.info("Person er bosatt:  ${request.bosatt}")
        return if (request.bosatt == Bosatt.NORGE) {
            if (request.avsenderLand != "DE" && request.fdato.ageIsBetween18and62()) {
                logger.info("${request.sedType} i ${request.bucType} gir enhet ${Enhet.UFORE_UTLANDSTILSNITT.enhetsNr} på grunn av bosatt norge, avsenderland ikke er DE og alder er mellom 18 og 62")
                Enhet.UFORE_UTLANDSTILSNITT
            }
            else {
                logger.info("${request.sedType} i ${request.bucType} gir enhet ${Enhet.NFP_UTLAND_AALESUND.enhetsNr} på grunn av bosatt norge og alder er NFP")
                Enhet.NFP_UTLAND_AALESUND
            }
        }
        else if (request.bosatt == Bosatt.UTLAND){
            if (request.fdato.ageIsBetween18and62()) {
                logger.info("${request.sedType} i ${request.bucType} gir enhet ${Enhet.UFORE_UTLAND.enhetsNr} på grunn av bosatt utland og alder er NAY")
                Enhet.UFORE_UTLAND
            }
            else {
                logger.info("${request.sedType} i ${request.bucType} gir enhet ${Enhet.PENSJON_UTLAND.enhetsNr} på grunn av bosatt utland og alder er NFP")
                Enhet.PENSJON_UTLAND
            }
        } else {
            logger.info("${request.sedType} i ${request.bucType} gir enhet ${Enhet.ID_OG_FORDELING.enhetsNr} på grunn av at bosatt er ukjent")
            Enhet.ID_OG_FORDELING
        }
    }


}

class EnhetFactory {
    companion object {
        fun hentHandlerFor(bucType: BucType): EnhetHandler {
            return when (bucType) {
                P_BUC_01 -> Pbuc01()
                P_BUC_02 -> Pbuc02()
                P_BUC_03 -> Pbuc03()
                P_BUC_04 -> Pbuc04()
                P_BUC_05 -> Pbuc05()
                P_BUC_06,
                P_BUC_07,
                P_BUC_08,
                P_BUC_09 -> DefaultEnhetHandler()
                P_BUC_10 -> Pbuc10()
                H_BUC_07 -> Hbuc07()
                R_BUC_02 -> Rbuc02()
                else -> DefaultEnhetHandler()
            }
        }
    }
}


fun LocalDate.ageIsBetween18and60(): Boolean {
    val age = Period.between(this, LocalDate.now())
    return (age.years >= 18) && (age.years < 60)
}

fun LocalDate.ageIsBetween18and62(): Boolean {
    val age = Period.between(this, LocalDate.now())
    return (age.years >= 18) && (age.years < 62)
}


