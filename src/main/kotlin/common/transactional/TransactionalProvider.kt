package com.example.common.transactional

import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction


enum class TransactionPropagation {
    REQUIRED,  // 트랜잭션을 생성 -> 없으면 생성
    REQUIRED_NEW, // 항상 새 트랜잭션을 생성
}

object TransactionProvider {

    suspend fun <T> transaction(
        propagation: TransactionPropagation = TransactionPropagation.REQUIRED,
        readOnly: Boolean = false,
        callFun: suspend () -> T
    ) : T {
        val current = TransactionManager.currentOrNull()

        return when(propagation) {
            TransactionPropagation.REQUIRED -> {
                if (current != null && !current.connection.isClosed) {
                    callFun()
                } else {
                    newSuspendedTransaction(Dispatchers.IO) {
                        applyReadOnly(readOnly) { callFun() }
                    }
                }
            }
            TransactionPropagation.REQUIRED_NEW -> {
                newSuspendedTransaction(Dispatchers.IO) {
                    applyReadOnly (readOnly){ callFun() }
                }
            }
        }
    }

    private suspend fun <T> Transaction.applyReadOnly(
        readOnly: Boolean = false,
        callFun: suspend () -> T
    ) : T {
        if (readOnly) {
            connection.readOnly = true
        }

        return callFun()
    }
}