package app.rootstock.data.channel

import androidx.room.*
import app.rootstock.data.db.DateConverter
import app.rootstock.data.workspace.Workspace
import com.google.gson.annotations.SerializedName
import java.util.*

interface ChannelI {
    val name: String
    val channelId: Long
    val lastMessage: String?
    var workspaceId: String?
    val imageUrl: String?
    val backgroundColor: String?
}

@Entity(
    tableName = "channels",
    foreignKeys = [ForeignKey(
        entity = Workspace::class,
        parentColumns = ["ws_id"],
        childColumns = ["workspace_id"]
    )]
)
@TypeConverters(DateConverter::class)
data class Channel(
    override val name: String,
    @PrimaryKey
    @ColumnInfo(name = "channel_id")
    @SerializedName("channel_id")
    override val channelId: Long,
    @ColumnInfo(name = "last_message")
    @SerializedName("last_message")
    override val lastMessage: String?,
    @ColumnInfo(name = "background_color")
    @SerializedName("background_color")
    override val backgroundColor: String,
    @ColumnInfo(name = "image_url")
    @SerializedName("image_url")
    override val imageUrl: String?,
    @ColumnInfo(name = "last_update")
    @SerializedName("last_update")
    val lastUpdate: Date,
    @ColumnInfo(name = "workspace_id")
    @SerializedName("workspace_id")
    override var workspaceId: String? = null
) : ChannelI

