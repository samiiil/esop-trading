package Models
class Inventory {
    private var freeInventory: Long=0L;
    private var lockedInventory: Long=0L;

    fun addEsopToInventory(esopsToBeAdded:Long){
        this.freeInventory =  this.freeInventory + esopsToBeAdded;
    }

    fun getFreeInventory(): Long {
        return this.freeInventory;
    }

    fun getLockedInventory(): Long {
        return this.lockedInventory;
    }

    fun updateLockedInventory( inventoryToBeUpdated: Long){
        this.lockedInventory = this.lockedInventory - inventoryToBeUpdated
    }
    fun moveFreeInventoryToLockedInventory(esopsToBeLocked:Long): String {
        if(this.freeInventory >= esopsToBeLocked){
            this.freeInventory = this.freeInventory - esopsToBeLocked;
            this.lockedInventory = this.lockedInventory + esopsToBeLocked;
            return "Success";
        }else{
            return "Insufficient ESOPs in Inventory.";
        }
    }
}