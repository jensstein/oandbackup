package com.machiav3lli.backup.utils

import kotlinx.coroutines.flow.Flow

object FlowUtils {

    fun <T1, T2, T3, T4, T5, T6, R> FlowUtils.combine(
        flow: Flow<T1>,
        flow2: Flow<T2>,
        flow3: Flow<T3>,
        flow4: Flow<T4>,
        flow5: Flow<T5>,
        flow6: Flow<T6>,
        transform: suspend (T1, T2, T3, T4, T5, T6) -> R
    ): Flow<R> = kotlinx.coroutines.flow.combine(
        kotlinx.coroutines.flow.combine(flow, flow2, flow3, ::Triple),
        kotlinx.coroutines.flow.combine(flow4, flow5, flow6, ::Triple)
    ) { t1, t2 ->
        transform(
            t1.first,
            t1.second,
            t1.third,
            t2.first,
            t2.second,
            t2.third
        )
    }

}
