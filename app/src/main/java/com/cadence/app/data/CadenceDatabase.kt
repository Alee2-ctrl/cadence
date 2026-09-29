package com.cadence.app.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        HabitEntity::class,
        HabitLogEntity::class,
        RoutineEntity::class,
        RoutineLogEntity::class,
        TaskEntity::class,
        ReviewEntity::class,
        NoteEntity::class,
        ModeEntity::class,
        LockoutEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class CadenceDatabase : RoomDatabase() {
    abstract fun habitDao(): HabitDao
    abstract fun habitLogDao(): HabitLogDao
    abstract fun routineDao(): RoutineDao
    abstract fun routineLogDao(): RoutineLogDao
    abstract fun taskDao(): TaskDao
    abstract fun reviewDao(): ReviewDao
    abstract fun noteDao(): NoteDao
    abstract fun modeDao(): ModeDao
    abstract fun lockoutDao(): LockoutDao

    companion object {
        @Volatile
        private var instance: CadenceDatabase? = null

        fun get(context: Context): CadenceDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    CadenceDatabase::class.java,
                    "cadence.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { instance = it }
            }
    }
}
