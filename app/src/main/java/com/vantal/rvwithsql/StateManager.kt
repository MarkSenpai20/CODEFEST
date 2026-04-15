package com.vantal.rvwithsql

object UIState {
    object FormButton{
        const val STATE_LOGIN = 1 //0001
        const val STATE_REGISTER = 2 //0010
    }

    object Navigation{
        const val STATE_HOME = 1 shl 2//0100
        const val STATE_DASHBOARD = 2 shl 2 //1000
        const val STATE_SETTINGS = 3 shl 2 //1100
    }

    object NavHome{
        object ProductList {
            const val HOME = Navigation.STATE_HOME //0100
            const val STATE_LIST = 1 or HOME //0101
            const val STATE_PREVIEW = 2 or HOME // 0110

            // Helper checking state if active
            fun isState(currentState: Int, state: Int) = (currentState and state) == state
        }
    }
}
