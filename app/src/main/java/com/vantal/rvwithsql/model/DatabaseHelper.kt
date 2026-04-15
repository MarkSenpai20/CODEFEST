package com.vantal.rvwithsql.model// DatabaseHelper.kt
import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "products.db"
        private const val DATABASE_VERSION = 3
        const val TABLE_PRODUCTS = "products"
        const val COL_ID = "_id"
        const val COL_NAME = "name"
        const val COL_DESC = "description"
        const val COL_PRICE = "price"
        const val COL_IMAGE = "image_path"


        const val USERS = "users"
        const val USER_ID = "userid"
        const val USER_EMAIL = "email"
        const val USER_PASSWORD = "password"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createUserTable = """
            CREATE TABLE $USERS (
                $USER_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $USER_EMAIL TEXT NOT NULL,
                $USER_PASSWORD TEXT NOT NULL)
        """.trimIndent()




        val createProductTable = """
            CREATE TABLE $TABLE_PRODUCTS (
                $COL_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_NAME TEXT NOT NULL,
                $COL_DESC TEXT NOT NULL,
                $COL_PRICE REAL NOT NULL,
                $COL_IMAGE TEXT NOT NULL
            )
        """.trimIndent()
        db.execSQL(createProductTable)
        db.execSQL(createUserTable)


        insertInitialProducts(db)
        insertSampleUser(db)
    }

    private fun insertSampleUser(db: SQLiteDatabase){
        val users = listOf(
            UserData("admin@gmail.com", "admin"),
            UserData("mj@gmail.com", "123")
        )

        for (user in users){
            val values = ContentValues().apply {
                put(USER_EMAIL, user.email)
                put(USER_PASSWORD, user.password)
            }
            db.insert(USERS, null, values)
        }
    }

    fun addUsers(email: String, password: String): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(USER_EMAIL, email)
            put(USER_PASSWORD, password)
        }
        return db.insert(USERS, null, values)
    }

    fun checkUsers(email: String, password: String): Boolean {
        val db = readableDatabase
        val selection = "$USER_EMAIL = ? AND $USER_PASSWORD = ?"
        val selectionArgs = arrayOf(email, password)

        val cursor = db.query(
            USERS,           // Table
            null,
            selection,
            selectionArgs,
            null,
            null,
            null
        )

        val exists = cursor.count > 0
        cursor.close()
        return exists
    }


    fun getUserByEmail(email: String): Users? {
        val db = readableDatabase
        val cursor = db.query(
            USERS,
            null,
            "$USER_EMAIL = ?",
            arrayOf(email),
            null, null, null
        )

        var user: Users? = null
        if (cursor.moveToFirst()) {
            val id = cursor.getLong(cursor.getColumnIndexOrThrow(USER_ID))
            val userEmail = cursor.getString(cursor.getColumnIndexOrThrow(USER_EMAIL))
            val password = cursor.getString(cursor.getColumnIndexOrThrow(USER_PASSWORD))
            user = Users(id, userEmail, password)
        }
        cursor.close()
        return user
    }


    fun getUserById(id: Long): Users? {
        val db = readableDatabase
        val cursor = db.query(
            USERS,
            null,
            "$USER_ID = ?",
            arrayOf(id.toString()),
            null, null, null
        )

        var user: Users? = null
        if (cursor.moveToFirst()) {
            val userId = cursor.getLong(cursor.getColumnIndexOrThrow(USER_ID))
            val email = cursor.getString(cursor.getColumnIndexOrThrow(USER_EMAIL))
            val pass = cursor.getString(cursor.getColumnIndexOrThrow(USER_PASSWORD))
            user = Users(userId, email, pass)
        }
        cursor.close()
        return user
    }

    private fun insertInitialProducts(db: SQLiteDatabase) {
        val products = listOf(
            ProductData("Smartphone", "Latest 5G phone", 699.99, "ic_launcher_foreground"),
            ProductData("Laptop", "16GB RAM, 512GB SSD", 1299.99, "ic_laptop"),
            ProductData("Headphones", "Noise cancelling", 199.99, "ic_headphones"),
            ProductData("Smartwatch", "Fitness tracker", 249.99, "ic_watch")
        )

        for (product in products) {
            val values = ContentValues().apply {
                put(COL_NAME, product.name)
                put(COL_DESC, product.description)
                put(COL_PRICE, product.price)
                put(COL_IMAGE, product.imagePath)
            }
            db.insert(TABLE_PRODUCTS, null, values)
        }
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_PRODUCTS")
        db.execSQL("DROP TABLE IF EXISTS $USERS")
        onCreate(db)
    }


    fun addProduct(name: String, desc: String, price: Double, imagePath: String): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_NAME, name)
            put(COL_DESC, desc)
            put(COL_PRICE, price)
            put(COL_IMAGE, imagePath)
        }
        return db.insert(TABLE_PRODUCTS, null, values)
    }


    fun getAllProducts(): List<Product> {
        val products = mutableListOf<Product>()
        val db = readableDatabase
        val cursor = db.query(TABLE_PRODUCTS, null, null, null, null, null, null)

        with(cursor) {
            while (moveToNext()) {
                val id = getLong(getColumnIndexOrThrow(COL_ID))
                val name = getString(getColumnIndexOrThrow(COL_NAME))
                val desc = getString(getColumnIndexOrThrow(COL_DESC))
                val price = getDouble(getColumnIndexOrThrow(COL_PRICE))
                val imagePath = getString(getColumnIndexOrThrow(COL_IMAGE))
                products.add(Product(id, name, desc, price, imagePath))
            }
        }
        cursor.close()
        db.close()
        return products
    }

    /**

     * @param sortBy: constants COL_NAME, COL_PRICE, ...
     * @param isAscending: true = A-Z/Lowest-First; false = Z-A/Highest-First
     */
    fun getSortedProducts(sortBy: String, isAscending: Boolean = true): List<Product> {
        val products = mutableListOf<Product>()
        val db = readableDatabase


        val order = if (isAscending) "ASC" else "DESC"
        val orderByClause = "$sortBy $order"

        val cursor = db.query(
            TABLE_PRODUCTS,  // Table
            null,
            null,
            null,
            null,
            null,
            orderByClause
        )

        with(cursor) {
            while (moveToNext()) {
                val id = getLong(getColumnIndexOrThrow(COL_ID))
                val name = getString(getColumnIndexOrThrow(COL_NAME))
                val desc = getString(getColumnIndexOrThrow(COL_DESC))
                val price = getDouble(getColumnIndexOrThrow(COL_PRICE))
                val imagePath = getString(getColumnIndexOrThrow(COL_IMAGE))
                products.add(Product(id, name, desc, price, imagePath))
            }
        }
        cursor.close()
        return products
    }

    // Internal data class
    private data class ProductData(val name: String, val description: String, val price: Double, val imagePath: String)
    private data class UserData(val email: String, val password: String)
}