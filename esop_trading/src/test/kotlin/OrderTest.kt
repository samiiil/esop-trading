import Models.DataStorage
import Services.Util
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class OrderTest {

    @BeforeEach
    fun `create user`(){
        Util.createUser("us1", "us1", "lastname", "1", "emailed")
    }
    //clean up should be done after each test
    @AfterEach
    fun `clear data`() {
        DataStorage.userList.clear()
        DataStorage.buyList.clear()
        DataStorage.sellList.clear()
        DataStorage.performanceSellList.clear()
        DataStorage.TOTAL_FEE_COLLECTED = 0
    }

    @Test
    fun `it should place the buy order`(){
        //arrange 3

        val user = DataStorage.userList["us1"]
        user!!.account.wallet.addMoneyToWallet(100)

        //act 1
        user.addOrder(1, "BUY", 100)

        //assert 2
        assertEquals(1, DataStorage.buyList.size)
        assertEquals(0, user.account.wallet.getFreeMoney())
    }

    @Test
    fun `It should place the sell order`(){
        //arrange 3
        val user = DataStorage.userList["us1"]
        user!!.account.inventory.addEsopToInventory(1)

        //act 1
        user.addOrder(1, "SELL", 100)

        //assert 2
        assertEquals(1, DataStorage.sellList.size)
        assertEquals(0, user.account.inventory.getFreeInventory())
    }

    @Test
    fun `it should match the existing non-performance sell order for equal price and quantity`(){
        //first user created in the before each block
        val userOne = DataStorage.userList["us1"]

        Util.createUser("us2", "us1", "lastname", "1", "emailed")
        val userTwo = DataStorage.userList["us2"]

        userOne!!.account.inventory.addEsopToInventory(1)
        userTwo!!.account.wallet.addMoneyToWallet(100)

        userOne.addOrder(1, "SELL", 100)
        userTwo.addOrder(1, "BUY", 100)

        assertEquals(0, DataStorage.sellList.size)
        assertEquals(0, DataStorage.buyList.size)
        assertEquals(0, userOne.account.inventory.getFreeInventory())
        assertEquals(1, userTwo.account.inventory.getFreeInventory())
        assertEquals(98, userOne.account.wallet.getFreeMoney())

    }

    @Test
    fun `sell order should match with existing multiple buy orders`(){
        val userOne = DataStorage.userList["us1"]
        Util.createUser("us2", "us1", "lastname", "1", "emailed")
        val userTwo = DataStorage.userList["us2"]
        userOne!!.account.wallet.addMoneyToWallet(300)
        userTwo!!.account.inventory.addEsopToInventory(3)

        for(i in 1..3)
            userOne.addOrder(1, "BUY", 100)

        userTwo.addOrder(3, "SELL", 100)

        assertEquals(0, DataStorage.sellList.size)
        assertEquals(0, DataStorage.buyList.size)
        assertEquals(0, userOne.account.wallet.getFreeMoney())
        assertEquals(3, userOne.account.inventory.getFreeInventory())
        assertEquals("Filled", userTwo.orders[0].orderStatus)
    }

    @Test
    fun `it should give first priority to performance ESOPS orders`(){
        val userOne = DataStorage.userList["us1"]
        Util.createUser("us2", "us1", "lastname", "1", "emailed")
        val userTwo = DataStorage.userList["us2"]
        userTwo!!.account.wallet.addMoneyToWallet(300)
        userOne!!.account.inventory.addEsopToInventory(6, "PERFORMANCE")

        userOne.addOrder(3, "SELL", 100)
        userOne.addOrder(3, "SELL", 100, "PERFORMANCE")
        userTwo.addOrder(3, "BUY", 100)

        assertEquals(0, DataStorage.sellList.size)
        assertEquals(0, DataStorage.buyList.size)
        assertEquals(3, userOne.account.inventory.getFreePerformanceInventory())
        assertEquals(3, userTwo.account.inventory.getFreeInventory())
        assertEquals(0, DataStorage.performanceSellList.size)

    }

    @Test
    fun `It should check performance fee should get deducted from seller wallet`(){
        val userOne = DataStorage.userList["us1"]
        Util.createUser("us2", "us1", "lastname", "1", "emailed")
        val userTwo = DataStorage.userList["us2"]
        userOne!!.account.inventory.addEsopToInventory(1)
        userTwo!!.account.wallet.addMoneyToWallet(300)

        userOne.addOrder(1, "SELL", 200)
        userTwo.addOrder(1, "BUY", 200)


        assertEquals(196, userOne.account.wallet.getFreeMoney())
        assertEquals("Filled", userOne.orders[0].orderStatus)
        assertEquals(4, DataStorage.TOTAL_FEE_COLLECTED)

    }

    @Test
    fun `it should check partial completion of order`(){
        val userOne = DataStorage.userList["us1"]
        Util.createUser("us2", "us1", "lastname", "1", "emailed")
        val userTwo = DataStorage.userList["us2"]
        userOne!!.account.inventory.addEsopToInventory(3)
        userTwo!!.account.wallet.addMoneyToWallet(100)


        userOne.addOrder(3, "SELL", 100)
        userTwo.addOrder(1, "BUY", 100)

        assertEquals("Partially Filled", userOne.orders[0].orderStatus)
        assertEquals(2, userOne.account.inventory.getLockedInventory())

    }

    @Test
    fun `it should get order details of user with `(){
        val user = DataStorage.userList["us1"]
        user!!.account.inventory.addEsopToInventory(100)

        val orderDetails = user.getOrderDetails()

        Assertions.assertTrue(orderDetails["order_history"]!!.isEmpty())
    }
}