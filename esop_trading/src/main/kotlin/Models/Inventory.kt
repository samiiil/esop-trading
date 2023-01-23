package Models

class Inventory {
    private var freeInventory: Long = 0L
    private var lockedInventory: Long = 0L
    private var freePerformanceInventory: Long = 0L
    private var lockedPerformanceInventory: Long = 0L

    fun addEsopToInventory(esopsToBeAdded: Long, type: String = "NON-PERFORMANCE") {
        if (type == "PERFORMANCE") {
            this.freePerformanceInventory = this.freePerformanceInventory + esopsToBeAdded
        } else {
            this.freeInventory = this.freeInventory + esopsToBeAdded
        }

    }

    fun getFreeInventory(): Long {
        return this.freeInventory
    }

    fun getLockedInventory(): Long {
        return this.lockedInventory
    }

    fun getFreePerformanceInventory(): Long {
        return this.freePerformanceInventory
    }

    fun getLockedPerformanceInventory(): Long {
        return this.lockedPerformanceInventory
    }


    fun updateLockedInventory(inventoryToBeUpdated: Long, isPerformanceESOP: Boolean) {
        if (isPerformanceESOP)
            this.lockedPerformanceInventory = this.lockedPerformanceInventory - inventoryToBeUpdated
        else
            this.lockedInventory = this.lockedInventory - inventoryToBeUpdated
    }

    fun moveFreeInventoryToLockedInventory(esopsToBeLocked: Long): String {
        if (this.freeInventory < esopsToBeLocked) {
            return "Insufficient ESOPs in Inventory."
        }
        this.freeInventory = this.freeInventory - esopsToBeLocked
        this.lockedInventory = this.lockedInventory + esopsToBeLocked
        return "Success"
    }

    fun moveFreePerformanceInventoryToLockedPerformanceInventory(esopsToBeLocked: Long): String {
        if (this.freePerformanceInventory < esopsToBeLocked) {
            return "Insufficient ESOPs in Inventory."
        }
        this.freePerformanceInventory = this.freePerformanceInventory - esopsToBeLocked
        this.lockedPerformanceInventory = this.lockedPerformanceInventory + esopsToBeLocked
        return "Success"
    }

}