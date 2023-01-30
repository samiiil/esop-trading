package models

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class TestUser {
    @BeforeEach
    fun cleanUp(){
        DataStorage.userList.clear()
        DataStorage.registeredEmails.clear()
        DataStorage.registeredPhoneNumbers.clear()
        DataStorage.buyList.clear()
        DataStorage.sellList.clear()
        DataStorage.performanceSellList.clear()
        DataStorage.orderId = 0
        DataStorage.orderExecutionId = 0
    }

    @Test
    fun `can create new valid user`(){
        val user = User(firstName = "user", lastName = "user", emailId = "user@example.com", phoneNumber = "+911234567890", username = "user")

        assertEquals(1, DataStorage.userList.size)
        assertEquals(user, DataStorage.userList["user"])
    }

    @Test
    fun `can create multiple users`(){
        val user1 = User(firstName = "user1", lastName = "user1", emailId = "user1@example.com", phoneNumber = "+911234567891", username = "user1")
        val user2 = User(firstName = "user2", lastName = "user2", emailId = "user2@example.com", phoneNumber = "+911234567892", username = "user2")
        val user3 = User(firstName = "user3", lastName = "user3", emailId = "user3@example.com", phoneNumber = "+911234567893", username = "user3")

        assertEquals(3, DataStorage.userList.size)
        assertEquals(user1, DataStorage.userList["user1"])
        assertEquals(user2, DataStorage.userList["user2"])
        assertEquals(user3, DataStorage.userList["user3"])
    }

    @Test
    fun `user email added to registered email list`(){
        User(firstName = "user", lastName = "user", emailId = "user@example.com", phoneNumber = "+911234567890", username = "user")

        assertEquals(1, DataStorage.registeredEmails.size)
        assert(DataStorage.registeredEmails.contains("user@example.com"))
    }

    @Test
    fun `user phone number added to registered phone number list`(){
        User(firstName = "user", lastName = "user", emailId = "user@example.com", phoneNumber = "+911234567890", username = "user")

        assertEquals(1, DataStorage.registeredEmails.size)
        assert(DataStorage.registeredPhoneNumbers.contains("+911234567890"))
    }

    @Test
    fun `user wallet and inventory are initially empty`(){
        val user = User(firstName = "user", lastName = "user", emailId = "user@example.com", phoneNumber = "+911234567890", username = "user")

        assertEquals(0, user.account.wallet.getFreeMoney())
        assertEquals(0, user.account.wallet.getLockedMoney())
        assertEquals(0, user.account.inventory.getFreeInventory())
        assertEquals(0, user.account.inventory.getLockedInventory())
        assertEquals(0, user.account.inventory.getFreePerformanceInventory())
        assertEquals(0, user.account.inventory.getLockedPerformanceInventory())
    }

    @Test
    fun `can add money to wallet`(){
        val user = User(firstName = "user", lastName = "user", emailId = "user@example.com", phoneNumber = "+911234567890", username = "user")

        user.account.wallet.addMoneyToWallet(100)

        assertEquals(100, user.account.wallet.getFreeMoney())
        assertEquals(0, user.account.wallet.getLockedMoney())
    }

    @Test
    fun `can add normal ESOPs to inventory`(){
        val user = User(firstName = "user", lastName = "user", emailId = "user@example.com", phoneNumber = "+911234567890", username = "user")

        user.account.inventory.addEsopToInventory(10)

        assertEquals(10, user.account.inventory.getFreeInventory())
        assertEquals(0, user.account.inventory.getLockedInventory())
        assertEquals(0, user.account.inventory.getFreePerformanceInventory())
        assertEquals(0, user.account.inventory.getLockedPerformanceInventory())
    }

    @Test
    fun `can add performance ESOPs to inventory`(){
        val user = User(firstName = "user", lastName = "user", emailId = "user@example.com", phoneNumber = "+911234567890", username = "user")

        user.account.inventory.addEsopToInventory(10, "PERFORMANCE")

        assertEquals(10, user.account.inventory.getFreePerformanceInventory())
        assertEquals(0, user.account.inventory.getLockedPerformanceInventory())
        assertEquals(0, user.account.inventory.getFreeInventory())
        assertEquals(0, user.account.inventory.getLockedInventory())
    }
    @Test
    fun `can create buy order if user has money in wallet`(){
        val user = User(firstName = "user", lastName = "user", emailId = "user@example.com", phoneNumber = "+911234567890", username = "user")
        user.account.wallet.addMoneyToWallet(100)

        val response = user.addOrder(1, "BUY", 100)

        assert(response.isEmpty())
        assertEquals(1, user.orders.size)
    }

    @Test
    fun `cannot create buy order if user doesn't have enough money in wallet`(){
        val user = User(firstName = "user", lastName = "user", emailId = "user@example.com", phoneNumber = "+911234567890", username = "user")

        val response = user.addOrder(1, "BUY", 100)

        assertEquals("Insufficient balance in wallet", response[0])
        assertEquals(0, user.account.wallet.getFreeMoney())
        assertEquals(0, user.account.wallet.getLockedMoney())
    }

    @Test
    fun `creating buy order moves money to locked wallet`(){
        val user = User(firstName = "user", lastName = "user", emailId = "user@example.com", phoneNumber = "+911234567890", username = "user")
        user.account.wallet.addMoneyToWallet(100)

        user.addOrder(1, "BUY", 100)

        assertEquals(0, user.account.wallet.getFreeMoney())
        assertEquals(100, user.account.wallet.getLockedMoney())
    }

    @Test
    fun `correct buy order is created`(){
        val user = User(firstName = "user", lastName = "user", emailId = "user@example.com", phoneNumber = "+911234567890", username = "user")
        user.account.wallet.addMoneyToWallet(100)

        user.addOrder(1, "BUY", 100)

        assertEquals("Unfilled", DataStorage.buyList.peek().orderStatus)
        assertEquals(1, DataStorage.buyList.peek().orderQuantity)
        assertEquals("BUY", DataStorage.buyList.peek().orderType)
        assertEquals(100, DataStorage.buyList.peek().orderPrice)
        assertEquals(0, DataStorage.buyList.peek().orderExecutionLogs.size)
    }

    @Test
    fun `creating buy order adds order to global buy list`(){
        val user = User(firstName = "user", lastName = "user", emailId = "user@example.com", phoneNumber = "+911234567890", username = "user")
        user.account.wallet.addMoneyToWallet(100)

        user.addOrder(1, "BUY", 100)

        assertEquals(1, DataStorage.buyList.size)
        assertEquals(user.orders[0], DataStorage.buyList.peek())
    }

    @Test
    fun `can create sell order if user has enough esops`(){
        val user = User(firstName = "user", lastName = "user", emailId = "user@example.com", phoneNumber = "+911234567890", username = "user")
        user.account.inventory.addEsopToInventory(1)

        val response = user.addOrder(1, "SELL", 100)

        assert(response.isEmpty())
        assertEquals(1, user.orders.size)
    }

    @Test
    fun `cannot create sell order if user doesn't have enough esops in inventory`(){
        val user = User(firstName = "user", lastName = "user", emailId = "user@example.com", phoneNumber = "+911234567890", username = "user")

        val response = user.addOrder(1, "SELL", 100)

        assertEquals("Insufficient ESOPs in Inventory", response[0])
        assertEquals(0, user.account.inventory.getFreeInventory())
        assertEquals(0, user.account.inventory.getLockedInventory())
    }

    @Test
    fun `creating sell order locks esops`(){
        val user = User(firstName = "user", lastName = "user", emailId = "user@example.com", phoneNumber = "+911234567890", username = "user")
        user.account.inventory.addEsopToInventory(1)

        user.addOrder(1, "SELL", 100)

        assertEquals(0, user.account.inventory.getFreeInventory())
        assertEquals(1, user.account.inventory.getLockedInventory())
    }

    @Test
    fun `correct sell order is created`(){
        val user = User(firstName = "user", lastName = "user", emailId = "user@example.com", phoneNumber = "+911234567890", username = "user")
        user.account.inventory.addEsopToInventory(1)

        user.addOrder(1, "SELL", 100)

        assertEquals("Unfilled", DataStorage.sellList.peek().orderStatus)
        assertEquals(1, DataStorage.sellList.peek().orderQuantity)
        assertEquals("SELL", DataStorage.sellList.peek().orderType)
        assertEquals(100, DataStorage.sellList.peek().orderPrice)
        assertEquals(0, DataStorage.sellList.peek().orderExecutionLogs.size)
    }

    @Test
    fun `creating sell order adds order to global sell list`(){
        val user = User(firstName = "user", lastName = "user", emailId = "user@example.com", phoneNumber = "+911234567890", username = "user")
        user.account.inventory.addEsopToInventory(1)

        user.addOrder(1, "SELL", 100)

        assertEquals(1, DataStorage.sellList.size)
        assertEquals(user.orders[0], DataStorage.sellList.peek())
    }

    @Test
    fun `can create performance sell order if user has enough performance esops`(){
        val user = User(firstName = "user", lastName = "user", emailId = "user@example.com", phoneNumber = "+911234567890", username = "user")
        user.account.inventory.addEsopToInventory(1, "PERFORMANCE")

        val response = user.addOrder(1, "SELL", 100, "PERFORMANCE")

        assert(response.isEmpty())
        assertEquals(1, user.orders.size)
    }

    @Test
    fun `cannot create performance sell order if user doesn't have enough performance esops in inventory`(){
        val user = User(firstName = "user", lastName = "user", emailId = "user@example.com", phoneNumber = "+911234567890", username = "user")

        val response = user.addOrder(1, "SELL", 100, "PERFORMANCE")

        assertEquals("Insufficient ESOPs in Inventory", response[0])
        assertEquals(0, user.account.inventory.getFreePerformanceInventory())
        assertEquals(0, user.account.inventory.getLockedPerformanceInventory())
    }

    @Test
    fun `creating performance sell order locks esops`(){
        val user = User(firstName = "user", lastName = "user", emailId = "user@example.com", phoneNumber = "+911234567890", username = "user")
        user.account.inventory.addEsopToInventory(1, "PERFORMANCE")

        user.addOrder(1, "SELL", 100, "PERFORMANCE")

        assertEquals(0, user.account.inventory.getFreePerformanceInventory())
        assertEquals(1, user.account.inventory.getLockedPerformanceInventory())
    }

    @Test
    fun `correct performance sell order is created`(){
        val user = User(firstName = "user", lastName = "user", emailId = "user@example.com", phoneNumber = "+911234567890", username = "user")
        user.account.inventory.addEsopToInventory(1, "PERFORMANCE")

        user.addOrder(1, "SELL", 100, "PERFORMANCE")

        assertEquals("Unfilled", DataStorage.performanceSellList.peek().orderStatus)
        assertEquals(1, DataStorage.performanceSellList.peek().orderQuantity)
        assertEquals("SELL", DataStorage.performanceSellList.peek().orderType)
        assertEquals(100, DataStorage.performanceSellList.peek().orderPrice)
        assertEquals(0, DataStorage.performanceSellList.peek().orderExecutionLogs.size)
    }

    @Test
    fun `creating performance sell order adds order to global performance sell list`(){
        val user = User(firstName = "user", lastName = "user", emailId = "user@example.com", phoneNumber = "+911234567890", username = "user")
        user.account.inventory.addEsopToInventory(1, "PERFORMANCE")

        user.addOrder(1, "SELL", 100, "PERFORMANCE")

        assertEquals(1, DataStorage.performanceSellList.size)
        assertEquals(user.orders[0], DataStorage.performanceSellList.peek())
    }

    @Test
    fun `order details is initially empty`(){
        val user = User(firstName = "user", lastName = "user", emailId = "user@example.com", phoneNumber = "+911234567890", username = "user")
        val orderDetails = user.getOrderDetails()

        assert(orderDetails.keys.contains("order_history"))
        assertEquals(0, orderDetails["order_history"]!!.size)
    }

    @Test
    fun `order details for unfilled order is set correctly`(){
        val user = User(firstName = "user", lastName = "user", emailId = "user@example.com", phoneNumber = "+911234567890", username = "user")
        user.account.wallet.addMoneyToWallet(100)
        user.addOrder(1, "BUY", 100)

        val orderDetails = user.getOrderDetails()

        assertEquals(1, orderDetails.size)
        assert(orderDetails.keys.contains("order_history"))
        assertEquals("{order_id=0, quantity=1, type=BUY, price=100, unfilled=[{price=100, quantity=1}]}", orderDetails["order_history"]!![0].toString())
    }

    @Test
    fun `order details for partially filled order is set correctly`(){
        val buyer = User(firstName = "user1", lastName = "user1", emailId = "user1@example.com", phoneNumber = "+911234567891", username = "user1")
        val seller = User(firstName = "user2", lastName = "user2", emailId = "user2@example.com", phoneNumber = "+911234567892", username = "user2")
        buyer.account.wallet.addMoneyToWallet(200)
        seller.account.inventory.addEsopToInventory(1)
        buyer.addOrder(2, "BUY",100)
        seller.addOrder(1, "SELL", 100)

        val orderDetails = buyer.getOrderDetails()

        assertEquals(1, orderDetails.size)
        assert(orderDetails.keys.contains("order_history"))
        println(orderDetails["order_history"])
        assertEquals("{order_id=0, quantity=2, type=BUY, price=100, partially_filled=[{price=100, quantity=1}], unfilled=[{price=100, quantity=1}]}",orderDetails["order_history"]!![0].toString())
    }

    @Test
    fun `order details for fully filled order is set correctly`(){
        val buyer = User(firstName = "user1", lastName = "user1", emailId = "user1@example.com", phoneNumber = "+911234567891", username = "user1")
        val seller = User(firstName = "user2", lastName = "user2", emailId = "user2@example.com", phoneNumber = "+911234567892", username = "user2")
        buyer.account.wallet.addMoneyToWallet(200)
        seller.account.inventory.addEsopToInventory(1)
        buyer.addOrder(2, "BUY",100)
        seller.addOrder(1, "SELL", 100)

        val orderDetails = seller.getOrderDetails()

        assertEquals(1, orderDetails.size)
        assert(orderDetails.keys.contains("order_history"))
        println(orderDetails["order_history"])
        assertEquals("{order_id=1, quantity=1, type=SELL, price=100, filled=[{price=100, quantity=1}]}",orderDetails["order_history"]!![0].toString())
    }
}