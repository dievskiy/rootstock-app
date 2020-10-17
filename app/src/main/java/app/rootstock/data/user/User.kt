package app.rootstock.data.user

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * User class
 */
@Entity
class User(
    @ColumnInfo(name = "user_id")
    @PrimaryKey(autoGenerate = true)
    val userId: Int? = null,
)

