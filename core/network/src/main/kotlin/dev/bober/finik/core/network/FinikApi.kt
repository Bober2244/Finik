package dev.bober.finik.core.network

import dev.bober.finik.core.network.dto.AnswerIn
import dev.bober.finik.core.network.dto.AnswerOut
import dev.bober.finik.core.network.dto.BonusIn
import dev.bober.finik.core.network.dto.BonusOut
import dev.bober.finik.core.network.dto.BuyIn
import dev.bober.finik.core.network.dto.BuyOut
import dev.bober.finik.core.network.dto.CareIn
import dev.bober.finik.core.network.dto.CareOut
import dev.bober.finik.core.network.dto.ChatIn
import dev.bober.finik.core.network.dto.ChatOut
import dev.bober.finik.core.network.dto.ChoiceOut
import dev.bober.finik.core.network.dto.ChooseIn
import dev.bober.finik.core.network.dto.CustomizeIn
import dev.bober.finik.core.network.dto.DayOut
import dev.bober.finik.core.network.dto.DepositIn
import dev.bober.finik.core.network.dto.DiaryOut
import dev.bober.finik.core.network.dto.DreamPlanOut
import dev.bober.finik.core.network.dto.EventOut
import dev.bober.finik.core.network.dto.GoalCatalogOut
import dev.bober.finik.core.network.dto.GoalIn
import dev.bober.finik.core.network.dto.HistoryOut
import dev.bober.finik.core.network.dto.LoginIn
import dev.bober.finik.core.network.dto.LoginOut
import dev.bober.finik.core.network.dto.OriginOut
import dev.bober.finik.core.network.dto.PetIn
import dev.bober.finik.core.network.dto.PlanIn
import dev.bober.finik.core.network.dto.ProfileOut
import dev.bober.finik.core.network.dto.QuestionOut
import dev.bober.finik.core.network.dto.QuizAnswerIn
import dev.bober.finik.core.network.dto.QuizAnswerOut
import dev.bober.finik.core.network.dto.QuizOut
import dev.bober.finik.core.network.dto.RemarkOut
import dev.bober.finik.core.network.dto.SettingsIn
import dev.bober.finik.core.network.dto.ShopItemOut
import dev.bober.finik.core.network.dto.StateOut
import dev.bober.finik.core.network.dto.TaskOut
import dev.bober.finik.core.network.dto.TermOut
import dev.bober.finik.core.network.dto.WeekSummaryOut
import dev.bober.finik.core.network.dto.WordOfDayOut
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText

class FinikApi(private val client: HttpClient) {

    suspend fun login(body: LoginIn): LoginOut =
        client.post("$V1/auth/device") { setBody(body) }.parse()

    suspend fun createPet(body: PetIn): StateOut =
        client.post("$V1/pet") { setBody(body) }.parse()

    suspend fun getState(): StateOut = client.get("$V1/state").parse()

    suspend fun customize(body: CustomizeIn): StateOut =
        client.post("$V1/pet/customize") { setBody(body) }.parse()

    suspend fun setPlan(body: PlanIn): StateOut =
        client.put("$V1/plan") { setBody(body) }.parse()

    suspend fun confirmPlan(): StateOut = client.post("$V1/plan/confirm").parse()

    suspend fun care(body: CareIn): CareOut =
        client.post("$V1/care") { setBody(body) }.parse()

    suspend fun selectGoal(body: GoalIn): StateOut =
        client.put("$V1/goal") { setBody(body) }.parse()

    suspend fun deposit(body: DepositIn): StateOut =
        client.post("$V1/goal/deposit") { setBody(body) }.parse()

    suspend fun endDay(): DayOut = client.post("$V1/day/end").parse()

    suspend fun shopItems(): List<ShopItemOut> = client.get("$V1/shop/items").parse()

    suspend fun buy(body: BuyIn): BuyOut =
        client.post("$V1/shop/buy") { setBody(body) }.parse()

    suspend fun tasks(): List<TaskOut> = client.get("$V1/tasks").parse()

    suspend fun questions(slug: String): List<QuestionOut> =
        client.get("$V1/tasks/$slug/questions").parse()

    suspend fun answer(slug: String, body: AnswerIn): AnswerOut =
        client.post("$V1/tasks/$slug/answer") { setBody(body) }.parse()

    suspend fun claim(slug: String): StateOut =
        client.post("$V1/tasks/$slug/claim").parse()

    suspend fun history(): HistoryOut = client.get("$V1/weeks/history").parse()

    suspend fun todayEvent(): EventOut? = client.get("$V1/events/today").parseOrNull()

    suspend fun chooseEvent(id: String, body: ChooseIn): ChoiceOut =
        client.post("$V1/events/$id/choose") { setBody(body) }.parse()

    suspend fun profile(): ProfileOut = client.get("$V1/profile").parse()

    suspend fun updateProfile(body: SettingsIn): StateOut =
        client.patch("$V1/profile") { setBody(body) }.parse()

    suspend fun badges(): List<dev.bober.finik.core.network.dto.BadgeOut> =
        client.get("$V1/profile/badges").parse()

    suspend fun reset() {
        client.post("$V1/profile/reset").throwIfError()
    }

    suspend fun parentBonus(body: BonusIn): StateOut =
        client.post("$V1/parent/bonus") { setBody(body) }.parse<BonusOut>().state

    suspend fun remark(): RemarkOut = client.get("$V1/ai/remark").parse()

    suspend fun weekSummary(): WeekSummaryOut = client.get("$V1/ai/week-summary").parse()

    suspend fun wordOfDay(): WordOfDayOut = client.get("$V1/ai/word-of-day").parse()

    suspend fun diary(): DiaryOut = client.get("$V1/ai/diary").parse()

    suspend fun dreamPlan(): DreamPlanOut = client.get("$V1/ai/dream-plan").parse()

    suspend fun origin(): OriginOut = client.get("$V1/ai/origin").parse()

    suspend fun quiz(kind: String = "quiz"): QuizOut =
        client.get("$V1/ai/quiz") { parameter("kind", kind) }.parse()

    suspend fun answerQuiz(body: QuizAnswerIn): QuizAnswerOut =
        client.post("$V1/ai/quiz/answer") { setBody(body) }.parse()

    suspend fun chat(body: ChatIn): ChatOut =
        client.post("$V1/ai/chat") { setBody(body) }.parse()

    suspend fun goalCatalog(): List<GoalCatalogOut> = client.get("$V1/content/goals").parse()

    suspend fun terms(): List<TermOut> = client.get("$V1/content/terms").parse()

    companion object {
        const val V1 = "api/v1"
    }
}

private suspend inline fun <reified T> HttpResponse.parse(): T {
    throwIfError()
    return body()
}

private suspend inline fun <reified T> HttpResponse.parseOrNull(): T? {
    throwIfError()
    val text = bodyAsText().trim()
    if (text.isEmpty() || text == "null") return null
    return FinikJson.json.decodeFromString(text)
}
