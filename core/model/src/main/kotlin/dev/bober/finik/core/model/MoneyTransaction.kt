package dev.bober.finik.core.model

/** A positive monetary amount with its source and the game period in which it moved. */
enum class TransactionKind { INCOME, PURCHASE, CARE, DEPOSIT, WITHDRAWAL }

data class MoneyTransaction(
    val week: Int,
    val kind: TransactionKind,
    val amount: Int,
    val source: String,
    val category: SpendCategory? = null,
    val goalId: String? = null,
)
