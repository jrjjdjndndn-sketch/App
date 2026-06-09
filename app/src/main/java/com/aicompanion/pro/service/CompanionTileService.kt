package com.aicompanion.pro.service

import android.content.Intent
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService

class CompanionTileService : TileService() {
    override fun onClick() {
        super.onClick()
        startService(
            Intent(this, CompanionService::class.java)
                .setAction(CompanionService.ACTION_MUTE)
        )
        qsTile?.apply {
            state = if (state == Tile.STATE_ACTIVE)
                Tile.STATE_INACTIVE else Tile.STATE_ACTIVE
            updateTile()
        }
    }
}
