package models

class Wallet {
    private var freeMoney: Long = 0L
    private var lockedMoney: Long = 0L

    fun addMoneyToWallet(amountToBeAdded: Long) {
        this.freeMoney = this.freeMoney + amountToBeAdded
    }

    fun getFreeMoney(): Long {
        return this.freeMoney
    }

    fun getLockedMoney(): Long {
        return this.lockedMoney
    }

    fun updateLockedMoney(amountToBeUpdated: Long) {
        this.lockedMoney = this.lockedMoney - amountToBeUpdated
    }

    fun moveFreeMoneyToLockedMoney(amountToBeLocked: Long): String {
        if (this.freeMoney >= amountToBeLocked) {
            this.freeMoney = this.freeMoney - amountToBeLocked
            this.lockedMoney = this.lockedMoney + amountToBeLocked
            return "Success"
        } else {
            return "Insufficient balance in wallet"
        }
    }
}