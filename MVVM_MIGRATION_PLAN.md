# MVVM Migration Plan for RMaps-ng

## EXECUTIVE SUMMARY

The RMaps-ng project has a **hybrid architecture**: modern Room database layer (Kotlin) exists but is completely disconnected from a **legacy Activity-based UI (Java)** with SharedPreferences-based state management. The migration to MVVM requires a phased approach, systematically replacing Activities with Fragment-based architecture, introducing ViewModels for state management, and connecting the UI to existing repositories.

**Migration Timeline Phases**: 4 major phases, startinletsg with data migration strategy

**Note**: Timeline estimates are approximate and may vary based on complexity discovered during implementation.

### Key Migration Pillars

1. **Data Migration First** (Phase 0): Seamlessly migrate user data from legacy SQLite to Room without data loss using dual-write pattern
2. **Foundation Before Features** (Phase 1): Establish Hilt DI, base MVVM components, and comprehensive testing infrastructure
3. **Simple to Complex** (Phase 2): Migrate POI/Track screens first to validate patterns, then tackle the 1,908-line MainActivity
4. **Reversible Changes** (All Phases): Feature flags enable instant rollback, legacy code coexists with new implementation
5. **Test Coverage** (All Phases): 80%+ coverage for ViewModels and Repositories ensures stability

### What's New in This Enhanced Plan

- ✅ **Phase 0: Data Migration Strategy** - Detailed dual-write pattern and migration infrastructure
- ✅ **Hilt Module Implementations** - Complete code examples for DI setup
- ✅ **Comprehensive Testing Strategy** - Repository integration tests, ViewModel unit tests, fake implementations
- ✅ **Navigation Component Integration** - Full navigation graph examples and fragment navigation patterns
- ✅ **MainActivity Decomposition Sequence** - Step-by-step extraction order with rationale
- ✅ **Rollback Strategy** - Feature flags, gradual rollout, emergency rollback procedures
- ✅ **Enhanced Risk Mitigation** - Expanded risk table with rollback options

---

## PHASE 0: DATA MIGRATION STRATEGY (Critical Pre-work)

**Goal**: Ensure seamless transition from legacy SQLite to Room without data loss.

### Current State Analysis

**Legacy Database Access**:
- `SQLiteSDOpenHelper` - handles track/POI data in legacy format
- `GeoDataDatabaseOpenHelper` - manages geographic data
- Direct SQL queries throughout Java codebase
- No centralized data access layer

**Room Database**:
- Version 23 (defined in `RMapsDatabase.kt`)
- Entities defined for Track, TrackPoint, POI, Category, Activity, Map
- DAOs implemented with Flow-based reactive queries
- **Critical**: Room database exists but is NOT connected to UI

### Migration Strategy: Dual-Write Pattern

**Approach**: Gradual migration using dual-write during transition period.

**Phase 0.1: Data Audit**
- [ ] Inventory all tables in legacy SQLite databases
- [ ] Map legacy schema to Room entities
- [ ] Identify data transformation requirements
- [ ] Document any data that cannot be migrated automatically

**Phase 0.2: Migration Infrastructure**
- [ ] Create `LegacyDataMigrator` utility class
- [ ] Implement one-time migration from legacy SQLite to Room
- [ ] Add migration version tracking in SharedPreferences
- [ ] Create rollback mechanism for failed migrations

**Phase 0.3: Dual-Write Adapter Pattern**
During transition, some screens will use legacy DB, others will use Room:

```kotlin
/**
 * Adapter that writes to both legacy SQLite and Room during migration
 * This ensures data consistency while screens are being migrated
 */
class DualWriteTrackRepository(
    private val legacyHelper: SQLiteSDOpenHelper,
    private val roomRepository: TrackRepository,
    private val migrationComplete: Boolean
) : ITrackRepository {

    override suspend fun insertTrack(track: Track): Long {
        return if (migrationComplete) {
            // Migration complete, only use Room
            roomRepository.insertTrack(track)
        } else {
            // During migration, write to both
            val roomId = roomRepository.insertTrack(track)
            val legacyId = legacyHelper.insertTrack(track.toLegacyFormat())
            // Return Room ID as authoritative
            roomId
        }
    }

    override fun getAllTracks(): Flow<List<Track>> {
        return if (migrationComplete) {
            roomRepository.getAllTracks()
        } else {
            // During migration, read from Room (it has the migrated data)
            roomRepository.getAllTracks()
        }
    }
}
```

**Phase 0.4: One-Time Data Migration**
```kotlin
class LegacyDataMigrator(
    private val context: Context,
    private val legacyHelper: SQLiteSDOpenHelper,
    private val roomDatabase: RMapsDatabase
) {
    suspend fun migrateAllData(): MigrationResult {
        return withContext(Dispatchers.IO) {
            try {
                val prefs = context.getSharedPreferences("migration_status", Context.MODE_PRIVATE)
                if (prefs.getBoolean("migration_complete", false)) {
                    return@withContext MigrationResult.AlreadyMigrated
                }

                // Migrate in transaction
                roomDatabase.runInTransaction {
                    migrateTracks()
                    migratePoiPoints()
                    migrateCategories()
                    migrateActivities()
                }

                // Mark migration complete
                prefs.edit().putBoolean("migration_complete", true).apply()

                MigrationResult.Success
            } catch (e: Exception) {
                MigrationResult.Failed(e)
            }
        }
    }

    private fun migrateTracks() {
        val cursor = legacyHelper.getAllTracksCursor()
        cursor.use {
            while (it.moveToNext()) {
                val track = it.toTrackEntity()
                roomDatabase.trackDao().insertTrackSync(track)
            }
        }
    }
}

sealed class MigrationResult {
    object Success : MigrationResult()
    object AlreadyMigrated : MigrationResult()
    data class Failed(val exception: Exception) : MigrationResult()
}
```

**Phase 0.5: Migration Execution Plan**
1. App startup checks migration status
2. If not migrated, show migration dialog to user
3. Run migration in background with progress indicator
4. On success, enable Room-based features
5. On failure, rollback and continue using legacy DB

**Rollback Strategy**:
- Keep legacy SQLite files intact during migration
- If migration fails, app continues using legacy DB
- User can retry migration later from settings
- Migration can be triggered manually from debug menu

**Files to Create**:
```
mainActivity/src/main/kotlin/com/sm/maps/applib/data/migration/
├── LegacyDataMigrator.kt
├── DualWriteTrackRepository.kt
├── DualWritePoiRepository.kt
├── MigrationResult.kt
└── LegacyCursorExtensions.kt (convert Cursor to Entity)
```

**Success Criteria**:
- [ ] All existing user data migrated to Room without loss
- [ ] Migration completes in reasonable time (<30 seconds for typical dataset)
- [ ] Rollback works if migration fails
- [ ] Both legacy and Room databases can coexist during transition

---

## PHASE 1: ESTABLISH MVVM FOUNDATION (Weeks 1-2)

**Goal**: Set up the infrastructure and patterns that all future screens will use.

### 1.1 Dependency Injection Setup
- [ ] Configure Hilt (already added, just needs activation)
  - Create `HiltApplication` in rMapsFree module
  - Add `@HiltAndroidApp` annotation
  - Configure module providers for database, DAOs, repositories
  - Add Hilt dependencies to Activities/Fragments
- [ ] Create Hilt modules for:
  - `DatabaseModule` - provide `RMapsDatabase` singleton
  - `RepositoryModule` - provide repository instances
  - `DispatcherModule` - provide Dispatcher.IO and Main

**Files to Create**:
```
mainActivity/src/main/kotlin/com/sm/maps/applib/di/
├── DatabaseModule.kt
├── RepositoryModule.kt
├── DispatcherModule.kt
└── HiltApplication.kt (in rMapsFree)
```

**Implementation Examples**:

**DatabaseModule.kt**:
```kotlin
package com.sm.maps.applib.di

import android.content.Context
import androidx.room.Room
import com.sm.maps.applib.data.local.database.RMapsDatabase
import com.sm.maps.applib.data.local.database.dao.CategoryDao
import com.sm.maps.applib.data.local.database.dao.PoiDao
import com.sm.maps.applib.data.local.database.dao.TrackDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideRMapsDatabase(
        @ApplicationContext context: Context
    ): RMapsDatabase {
        return Room.databaseBuilder(
            context,
            RMapsDatabase::class.java,
            "rmaps.db"
        )
            .addMigrations(*RMapsDatabase.MIGRATIONS.toTypedArray())
            .fallbackToDestructiveMigration() // Remove in production
            .build()
    }

    @Provides
    fun provideTrackDao(database: RMapsDatabase): TrackDao {
        return database.trackDao()
    }

    @Provides
    fun providePoiDao(database: RMapsDatabase): PoiDao {
        return database.poiDao()
    }

    @Provides
    fun provideCategoryDao(database: RMapsDatabase): CategoryDao {
        return database.categoryDao()
    }
}
```

**RepositoryModule.kt**:
```kotlin
package com.sm.maps.applib.di

import com.sm.maps.applib.data.repository.PoiRepository
import com.sm.maps.applib.data.repository.TrackRepository
import com.sm.maps.applib.domain.repository.IPoiRepository
import com.sm.maps.applib.domain.repository.ITrackRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindTrackRepository(
        trackRepository: TrackRepository
    ): ITrackRepository

    @Binds
    @Singleton
    abstract fun bindPoiRepository(
        poiRepository: PoiRepository
    ): IPoiRepository
}
```

**DispatcherModule.kt**:
```kotlin
package com.sm.maps.applib.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class IoDispatcher

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class MainDispatcher

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class DefaultDispatcher

@Module
@InstallIn(SingletonComponent::class)
object DispatcherModule {

    @Provides
    @IoDispatcher
    fun provideIoDispatcher(): CoroutineDispatcher = Dispatchers.IO

    @Provides
    @MainDispatcher
    fun provideMainDispatcher(): CoroutineDispatcher = Dispatchers.Main

    @Provides
    @DefaultDispatcher
    fun provideDefaultDispatcher(): CoroutineDispatcher = Dispatchers.Default
}
```

**Update MapApplication.java → MapApplication.kt**:
```kotlin
package com.sm.maps.applib

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import java.util.Locale

@HiltAndroidApp
class MapApplication : Application() {

    private lateinit var defLocale: Locale

    override fun onCreate() {
        super.onCreate()
        defLocale = Locale.getDefault()
    }

    fun getDefLocale(): Locale = defLocale
}
```

### 1.2 Create Base MVVM Components
- [ ] Create base ViewModel class with common functionality
- [ ] Create base Fragment with common lifecycle handling
- [ ] Setup sealed class for UI state management
  - `Loading`, `Success(data)`, `Error(exception)`, `Empty`
- [ ] Create reusable event handling pattern (one-shot events)

**Files to Create**:
```
mainActivity/src/main/kotlin/com/sm/maps/applib/presentation/
├── base/
│   ├── BaseViewModel.kt
│   ├── BaseFragment.kt
│   └── UiState.kt
├── viewmodel/
│   └── (ViewModels will go here)
├── ui/
│   └── (Fragments will go here)
└── state/
    └── (State classes)
```

### 1.3 Enhance Room Repositories
- [ ] Add missing repositories (for Categories, Activities, Maps)
- [ ] Implement caching strategy (optional: in-memory cache)
- [ ] Add error handling and logging
- [ ] Create repository interfaces in `domain/repository/`

**New Repositories**:
- `CategoryRepository`
- `ActivityRepository`
- `MapRepository`

### 1.4 Create Domain Models
- [ ] Move existing domain models (Track, PoiPoint) to dedicated package
- [ ] Create missing domain models for UI state
- [ ] Define repository interfaces for all data sources

**Files to Create**:
```
mainActivity/src/main/kotlin/com/sm/maps/applib/domain/
├── model/
│   ├── Track.kt
│   ├── PoiPoint.kt
│   ├── Category.kt
│   ├── Activity.kt
│   └── MapData.kt
└── repository/
    ├── ITrackRepository.kt
    ├── IPoiRepository.kt
    ├── ICategoryRepository.kt
    ├── IActivityRepository.kt
    └── IMapRepository.kt
```

### 1.5 Setup Testing Infrastructure

**Testing Strategy Overview**:
- **Unit Tests**: ViewModels, Repositories, Mappers, Use Cases
- **Integration Tests**: DAOs with in-memory Room database
- **Instrumentation Tests** (Optional): UI tests for critical flows
- **Target Coverage**: 80%+ for Kotlin code (ViewModels, Repositories, Domain layer)

**Dependencies** (Already in build.gradle):
```gradle
// Unit Testing
testImplementation 'junit:junit:4.13.2'
testImplementation 'org.mockito:mockito-core:5.2.0'
testImplementation "androidx.room:room-testing:$room_version"
testImplementation "org.jetbrains.kotlinx:kotlinx-coroutines-test:$coroutines_version"

// Additional testing utilities (to add)
testImplementation 'app.cash.turbine:turbine:1.0.0' // Flow testing
testImplementation 'com.google.truth:truth:1.1.5' // Assertions
testImplementation "androidx.arch.core:core-testing:2.2.0" // InstantTaskExecutorRule
```

**1.5.1 Repository Integration Tests**

Test repositories with in-memory Room database:

```kotlin
@RunWith(JUnit4::class)
class TrackRepositoryTest {

    private lateinit var database: RMapsDatabase
    private lateinit var trackDao: TrackDao
    private lateinit var repository: TrackRepository

    @Before
    fun setup() {
        // Create in-memory database
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RMapsDatabase::class.java
        ).allowMainThreadQueries().build()

        trackDao = database.trackDao()
        repository = TrackRepository(trackDao, Dispatchers.Unconfined)
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun `insertTrack should return track id`() = runTest {
        val track = Track(
            id = 0,
            name = "Test Track",
            timestamp = Date(),
            points = emptyList()
        )

        val id = repository.insertTrack(track)

        Truth.assertThat(id).isGreaterThan(0)
    }

    @Test
    fun `getAllTracks should emit tracks as Flow`() = runTest {
        // Insert test data
        val track1 = Track(id = 0, name = "Track 1", timestamp = Date())
        val track2 = Track(id = 0, name = "Track 2", timestamp = Date())
        repository.insertTrack(track1.toEntity())
        repository.insertTrack(track2.toEntity())

        // Collect Flow and verify
        repository.getAllTracks().test {
            val tracks = awaitItem()
            Truth.assertThat(tracks).hasSize(2)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
```

**1.5.2 ViewModel Unit Tests**

Test ViewModels with fake repositories:

```kotlin
@RunWith(JUnit4::class)
class PoiListViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var fakePoiRepository: FakePoiRepository
    private lateinit var viewModel: PoiListViewModel

    @Before
    fun setup() {
        fakePoiRepository = FakePoiRepository()
        viewModel = PoiListViewModel(fakePoiRepository, Dispatchers.Unconfined)
    }

    @Test
    fun `initial state should be Loading`() = runTest {
        viewModel.poiListState.test {
            Truth.assertThat(awaitItem()).isInstanceOf(UiState.Loading::class.java)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `loadPois should emit Success with data`() = runTest {
        // Given: repository has POIs
        val pois = listOf(
            PoiPoint(id = 1, name = "POI 1", lat = 0.0, lon = 0.0),
            PoiPoint(id = 2, name = "POI 2", lat = 0.0, lon = 0.0)
        )
        fakePoiRepository.setPois(pois)

        // When: ViewModel loads POIs
        viewModel.loadPois()

        // Then: state should be Success
        viewModel.poiListState.test {
            val state = awaitItem()
            Truth.assertThat(state).isInstanceOf(UiState.Success::class.java)
            Truth.assertThat((state as UiState.Success).data).hasSize(2)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `loadPois should emit Error on failure`() = runTest {
        // Given: repository throws exception
        fakePoiRepository.setShouldThrowError(true)

        // When: ViewModel loads POIs
        viewModel.loadPois()

        // Then: state should be Error
        viewModel.poiListState.test {
            Truth.assertThat(awaitItem()).isInstanceOf(UiState.Error::class.java)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
```

**1.5.3 Test Doubles and Fakes**

Create fake repositories for testing:

```kotlin
class FakePoiRepository : IPoiRepository {
    private val pois = mutableListOf<PoiPoint>()
    private var shouldThrowError = false

    fun setPois(poiList: List<PoiPoint>) {
        pois.clear()
        pois.addAll(poiList)
    }

    fun setShouldThrowError(value: Boolean) {
        shouldThrowError = value
    }

    override fun getAllPois(): Flow<List<PoiPoint>> = flow {
        if (shouldThrowError) {
            throw RuntimeException("Test error")
        }
        emit(pois.toList())
    }

    override suspend fun insertPoi(poi: PoiPoint): Long {
        if (shouldThrowError) throw RuntimeException("Test error")
        pois.add(poi)
        return poi.id
    }

    override suspend fun deletePoi(poiId: Long) {
        if (shouldThrowError) throw RuntimeException("Test error")
        pois.removeIf { it.id == poiId }
    }
}

class FakeTrackRepository : ITrackRepository {
    // Similar implementation for tracks
}
```

**Files to Create**:
```
mainActivity/src/test/kotlin/com/sm/maps/applib/
├── data/
│   └── repository/
│       ├── TrackRepositoryTest.kt
│       ├── PoiRepositoryTest.kt
│       └── CategoryRepositoryTest.kt
├── presentation/
│   └── viewmodel/
│       ├── PoiListViewModelTest.kt
│       ├── TrackListViewModelTest.kt
│       └── MapViewModelTest.kt
└── testutil/
    ├── FakePoiRepository.kt
    ├── FakeTrackRepository.kt
    └── TestCoroutineRule.kt
```

**Testing Checklist**:
- [ ] Add Turbine library for Flow testing
- [ ] Add Truth library for readable assertions
- [ ] Create fake repository implementations
- [ ] Write repository integration tests (with in-memory Room)
- [ ] Write ViewModel unit tests (with fake repositories)
- [ ] Setup CI to run tests on every commit
- [ ] Achieve 80%+ code coverage for ViewModels and Repositories

---

## PHASE 2: MIGRATE HIGH-VALUE SCREENS (Weeks 3-4)

**Target**: Start with POI and Track lists (simpler CRUD operations), then maps screen (most complex)

### 2.1 Migrate POI List Screen
**Current**: `PoiListActivity` (extends ListActivity, legacy Loader pattern)
**Target**: Fragment + ViewModel with Room repository

**Steps**:
1. Create `PoiListViewModel`
   - Inject `IPoiRepository`
   - Expose `Flow<List<PoiPoint>>` via StateFlow
   - Handle filtering/sorting
   - Implement delete/insert operations

2. Create `PoiListFragment`
   - Replace `ListActivity` with Fragment
   - Use `RecyclerView` instead of ListView
   - Implement proper lifecycle handling
   - Replace Cursor adapter with Flow-based adapter

3. Create `PoiListActivity` (host Activity)
   - Minimal Activity that hosts the Fragment
   - Only handles navigation

**Benefits of Starting Here**:
- Simple list CRUD operations
- Existing Room DAO already works
- Good learning ground for MVVM patterns
- Can test independently

### 2.2 Migrate Track List & Details Screen
**Current**: `TrackListActivity` + `TrackActivity`
**Target**: `TrackListFragment` + `TrackDetailsFragment` with shared ViewModel

**Steps**:
1. Create `TrackViewModel` (shared between list and details)
   - Expose tracks as `Flow<List<Track>>`
   - Load track details
   - Handle track updates/deletes
   - Track recording state management

2. Create Fragment pair:
   - `TrackListFragment` (shows list)
   - `TrackDetailsFragment` (shows/edits details)
   - Use shared ViewModel via `activityViewModels()`

3. Navigation:
   - Implement Navigation Component for fragment navigation
   - Handle back stack properly

**Navigation Architecture**:

**Add Navigation Component Dependencies** (build.gradle):
```gradle
// Navigation Component
def nav_version = "2.7.6"
implementation "androidx.navigation:navigation-fragment-ktx:$nav_version"
implementation "androidx.navigation:navigation-ui-ktx:$nav_version"
```

**Navigation Graph Structure** (`res/navigation/nav_graph.xml`):
```xml
<?xml version="1.0" encoding="utf-8"?>
<navigation xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:id="@+id/nav_graph"
    app:startDestination="@id/poiListFragment">

    <!-- POI Flow -->
    <fragment
        android:id="@+id/poiListFragment"
        android:name="com.sm.maps.applib.presentation.ui.poi.PoiListFragment"
        android:label="POI List">
        <action
            android:id="@+id/action_poi_list_to_details"
            app:destination="@id/poiDetailsFragment"
            app:enterAnim="@anim/slide_in_right"
            app:exitAnim="@anim/slide_out_left"
            app:popEnterAnim="@anim/slide_in_left"
            app:popExitAnim="@anim/slide_out_right" />
    </fragment>

    <fragment
        android:id="@+id/poiDetailsFragment"
        android:name="com.sm.maps.applib.presentation.ui.poi.PoiDetailsFragment"
        android:label="POI Details">
        <argument
            android:name="poiId"
            app:argType="long"
            android:defaultValue="-1L" />
    </fragment>

    <!-- Track Flow -->
    <fragment
        android:id="@+id/trackListFragment"
        android:name="com.sm.maps.applib.presentation.ui.track.TrackListFragment"
        android:label="Track List">
        <action
            android:id="@+id/action_track_list_to_details"
            app:destination="@id/trackDetailsFragment" />
    </fragment>

    <fragment
        android:id="@+id/trackDetailsFragment"
        android:name="com.sm.maps.applib.presentation.ui.track.TrackDetailsFragment"
        android:label="Track Details">
        <argument
            android:name="trackId"
            app:argType="long" />
    </fragment>

    <!-- Map Flow -->
    <fragment
        android:id="@+id/mapFragment"
        android:name="com.sm.maps.applib.presentation.ui.map.MapViewFragment"
        android:label="Map" />
</navigation>
```

**Fragment Navigation Example**:
```kotlin
// In PoiListFragment - navigate to details
override fun onPoiSelected(poi: PoiPoint) {
    val action = PoiListFragmentDirections.actionPoiListToDetails(poi.id)
    findNavController().navigate(action)
}

// In PoiDetailsFragment - receive argument
private val args: PoiDetailsFragmentArgs by navArgs()

override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    val poiId = args.poiId
    viewModel.loadPoi(poiId)
}

// Navigate back
findNavController().navigateUp()
```

**Host Activity Setup**:
```kotlin
@AndroidEntryPoint
class PoiActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_poi)

        // NavController is automatically found from NavHostFragment in layout
        val navController = findNavController(R.id.nav_host_fragment)
        setupActionBarWithNavController(navController)
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}
```

**Layout** (`activity_poi.xml`):
```xml
<androidx.constraintlayout.widget.ConstraintLayout>
    <androidx.fragment.app.FragmentContainerView
        android:id="@+id/nav_host_fragment"
        android:name="androidx.navigation.fragment.NavHostFragment"
        app:navGraph="@navigation/nav_graph"
        app:defaultNavHost="true"
        android:layout_width="match_parent"
        android:layout_height="match_parent" />
</androidx.constraintlayout.widget.ConstraintLayout>
```

### 2.3 Migrate Maps Screen (MainActivity) - MOST COMPLEX
**Current**: Monolithic 1,909-line Java Activity
**Strategy**: Break into multiple Fragments with clear responsibilities

**Sub-screens to create**:
1. `MapViewFragment` - Core map display
   - ViewModel: `MapViewModel`
   - State: current zoom, position, overlays, tile source
   - Responsibilities: map rendering, gesture handling

2. `OverlayControlFragment` - Overlay management
   - ViewModel: `OverlayViewModel`
   - State: which overlays are visible (tracks, POI, traffic, etc.)

3. `LocationTrackingFragment` - GPS/location features
   - ViewModel: `LocationViewModel`
   - State: current location, heading, following mode
   - Responsibilities: location updates, compass

4. `MeasureToolFragment` - Measure functionality
   - ViewModel: `MeasureToolViewModel`
   - State: measurement points, distance calculation

5. `MapSourceSelectionFragment` - Tile source management
   - ViewModel: `MapSourceViewModel`
   - State: available sources, current selection

**Integration Steps**:
1. Create parent `MainActivity` that hosts fragments
2. Create shared `MapCoordinatorViewModel` for cross-fragment communication
3. Use Shared ViewModel pattern: child fragments observe parent ViewModel
4. Replace Handler messages with ViewModel LiveData/StateFlow events

**State Migration Example**:
```kotlin
// BEFORE: SharedPreferences in MainActivity
SharedPreferences uiState = getPreferences(MODE_PRIVATE);
int lat = uiState.getInt("Latitude", 0);

// AFTER: ViewModel StateFlow
data class MapViewState(
    val latitude: Double,
    val longitude: Double,
    val zoomLevel: Int,
    val overlaysVisible: Map<String, Boolean>
)

class MapViewModel : BaseViewModel() {
    private val _mapState = MutableStateFlow(MapViewState(...))
    val mapState: StateFlow<MapViewState> = _mapState.asStateFlow()
}
```

**MainActivity Decomposition Sequence**:

The 1,908-line MainActivity.java must be broken down systematically to avoid breaking existing functionality. Follow this sequence:

**Step 1: Extract LocationTrackingFragment** (Week 3, Day 1-2)
- **Why first**: Most independent feature, minimal dependencies on map state
- **Responsibilities**: GPS tracking, location updates, compass, "follow me" mode
- **Extract from MainActivity**:
  - Location listener registration
  - Compass sensor handling
  - Location update UI (current position display)
  - "Center on location" functionality
- **ViewModel**: `LocationViewModel`
  ```kotlin
  data class LocationState(
      val currentLocation: Location?,
      val heading: Float,
      val isFollowingLocation: Boolean,
      val accuracy: Float
  )
  ```
- **Communication**: Publishes location updates to `MapCoordinatorViewModel`

**Step 2: Extract MeasureToolFragment** (Week 3, Day 3)
- **Why second**: Self-contained tool, doesn't affect main map rendering
- **Responsibilities**: Distance/area measurement, measurement overlay
- **Extract from MainActivity**:
  - Measurement point collection
  - Distance calculation logic
  - Measurement overlay rendering
- **ViewModel**: `MeasureToolViewModel`
  ```kotlin
  data class MeasureState(
      val points: List<GeoPoint>,
      val totalDistance: Double,
      val isActive: Boolean
  )
  ```

**Step 3: Extract MapSourceSelectionFragment** (Week 3, Day 4)
- **Why third**: Affects tile loading, but doesn't change frequently
- **Responsibilities**: Tile source picker, online/offline map selection
- **Extract from MainActivity**:
  - Tile provider initialization
  - Map source selection dialog
  - Tile cache management
- **ViewModel**: `MapSourceViewModel`
  ```kotlin
  data class MapSourceState(
      val availableSources: List<TileSource>,
      val currentSource: TileSource,
      val isOnline: Boolean
  )
  ```

**Step 4: Extract OverlayControlFragment** (Week 4, Day 1-2)
- **Why fourth**: Depends on map state, controls what's visible
- **Responsibilities**: Manage overlay visibility (tracks, POI, traffic, grid)
- **Extract from MainActivity**:
  - Overlay toggle controls
  - Overlay list management
  - Overlay rendering coordination
- **ViewModel**: `OverlayViewModel`
  ```kotlin
  data class OverlayState(
      val tracksVisible: Boolean,
      val poisVisible: Boolean,
      val gridVisible: Boolean,
      val trafficVisible: Boolean,
      val activeOverlays: Set<String>
  )
  ```
- **Communication**: Notifies `MapViewModel` of overlay changes

**Step 5: Extract Core MapViewFragment** (Week 4, Day 3-5)
- **Why last**: Core rendering engine, most complex, depends on all other components
- **Responsibilities**: Map rendering, gesture handling, zoom/pan
- **Extract from MainActivity**:
  - MapView initialization and configuration
  - Touch event handling (pan, zoom, double-tap)
  - Map center/zoom state management
  - Tile rendering coordination
- **ViewModel**: `MapViewModel`
  ```kotlin
  data class MapViewState(
      val center: GeoPoint,
      val zoomLevel: Int,
      val rotation: Float,
      val tileSource: TileSource,
      val isLoading: Boolean
  )
  ```
- **Communication**: Observes all other ViewModels via `MapCoordinatorViewModel`

**Step 6: Create MapCoordinatorViewModel** (Throughout Week 3-4)
- **Purpose**: Orchestrate communication between all map fragments
- **Pattern**: Shared ViewModel scoped to MainActivity
- **Responsibilities**:
  - Aggregate state from all child fragments
  - Coordinate actions that affect multiple fragments
  - Manage global map events (e.g., center changed, zoom changed)

```kotlin
@HiltViewModel
class MapCoordinatorViewModel @Inject constructor(
    private val trackRepository: ITrackRepository,
    private val poiRepository: IPoiRepository
) : BaseViewModel() {

    // Aggregated state
    private val _coordinatorState = MutableStateFlow(MapCoordinatorState())
    val coordinatorState: StateFlow<MapCoordinatorState> = _coordinatorState.asStateFlow()

    // Events from fragments
    private val _mapEvents = MutableSharedFlow<MapEvent>()
    val mapEvents: SharedFlow<MapEvent> = _mapEvents.asSharedFlow()

    // Actions
    fun onLocationChanged(location: Location) {
        viewModelScope.launch {
            _mapEvents.emit(MapEvent.LocationUpdated(location))
        }
    }

    fun onOverlayToggled(overlayId: String, visible: Boolean) {
        viewModelScope.launch {
            _mapEvents.emit(MapEvent.OverlayVisibilityChanged(overlayId, visible))
        }
    }

    fun onMapCenterChanged(center: GeoPoint) {
        _coordinatorState.update { it.copy(mapCenter = center) }
    }
}

data class MapCoordinatorState(
    val mapCenter: GeoPoint = GeoPoint(0.0, 0.0),
    val currentLocation: Location? = null,
    val activeOverlays: Set<String> = emptySet()
)

sealed class MapEvent {
    data class LocationUpdated(val location: Location) : MapEvent()
    data class OverlayVisibilityChanged(val overlayId: String, val visible: Boolean) : MapEvent()
    data class MapCenterChanged(val center: GeoPoint) : MapEvent()
}
```

**Fragment Communication Pattern**:
```kotlin
// In child fragments (LocationTrackingFragment, OverlayControlFragment, etc.)
class LocationTrackingFragment : BaseFragment() {

    private val locationViewModel: LocationViewModel by viewModels()
    private val coordinatorViewModel: MapCoordinatorViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Observe local ViewModel
        viewLifecycleOwner.lifecycleScope.launch {
            locationViewModel.locationState.collect { state ->
                // Update local UI
                updateLocationUI(state)

                // Notify coordinator of location changes
                state.currentLocation?.let { location ->
                    coordinatorViewModel.onLocationChanged(location)
                }
            }
        }

        // Observe coordinator events (if needed)
        viewLifecycleOwner.lifecycleScope.launch {
            coordinatorViewModel.mapEvents.collect { event ->
                when (event) {
                    is MapEvent.MapCenterChanged -> handleMapCenterChanged(event.center)
                    else -> {}
                }
            }
        }
    }
}
```

**Migration Validation Checklist**:
- [ ] Each extracted fragment works independently in isolation
- [ ] Fragment communication through ViewModel only (no direct fragment-to-fragment calls)
- [ ] No memory leaks (use viewLifecycleOwner for coroutines)
- [ ] Configuration changes don't lose state (ViewModel retention)
- [ ] Back button behavior preserved
- [ ] All existing MainActivity features work in new architecture
- [ ] Performance is equal or better (measure frame rate)

---

## PHASE 3: MIGRATE REMAINING SCREENS & REFACTOR (Weeks 5-6)

### 3.1 Migrate Settings/Preferences
**Current**: `MainPreferences` (extends PreferenceActivity)
**Target**: `SettingsFragment` with PreferenceDataStore

**Steps**:
1. Create `SettingsViewModel`
2. Migrate to `PreferenceFragmentCompat` (modern approach)
3. Replace SharedPreferences with DataStore (or keep SharedPreferences with wrapper)

### 3.2 Migrate Downloader Screens
**Current**: `DownloaderActivity`, `AreaSelectorActivity`
**Target**: Multi-step Fragment flow

**Steps**:
1. Create `MapDownloadViewModel`
2. Create Fragment sequence:
   - `DownloadSelectionFragment`
   - `AreaSelectionFragment`
   - `DownloadProgressFragment`

### 3.3 Refactor & Cleanup
- [ ] Remove all Activities except host Activities
- [ ] Remove legacy Loader pattern usage
- [ ] Remove SharedPreferences state management (migrate to ViewModel + optional DataStore)
- [ ] Remove Handler-based messaging
- [ ] Clean up legacy code

---

## DETAILED ARCHITECTURE AFTER MIGRATION

```
com.sm.maps.applib/
├── di/                                    # Dependency Injection (NEW)
│   ├── DatabaseModule.kt
│   ├── RepositoryModule.kt
│   ├── DispatcherModule.kt
│   └── ViewModelModule.kt
│
├── domain/                                # Domain Layer (ENHANCED)
│   ├── model/
│   │   ├── Track.kt
│   │   ├── PoiPoint.kt
│   │   ├── Category.kt
│   │   ├── Activity.kt
│   │   └── MapData.kt
│   ├── repository/
│   │   ├── ITrackRepository.kt
│   │   ├── IPoiRepository.kt
│   │   ├── ICategoryRepository.kt
│   │   ├── IActivityRepository.kt
│   │   └── IMapRepository.kt
│   └── usecase/                           # New: Domain use cases (optional)
│       └── (UseCases for complex business logic)
│
├── data/                                  # Data Layer (EXISTING + ENHANCED)
│   ├── local/database/
│   │   ├── RMapsDatabase.kt
│   │   ├── entity/
│   │   ├── dao/
│   │   ├── migration/
│   │   └── Converters.kt
│   ├── mapper/
│   │   ├── TrackMappers.kt
│   │   ├── PoiMappers.kt
│   │   └── (Additional mappers)
│   └── repository/                        # Implementation of domain repositories
│       ├── TrackRepository.kt
│       ├── PoiRepository.kt
│       ├── CategoryRepository.kt (NEW)
│       ├── ActivityRepository.kt (NEW)
│       └── MapRepository.kt (NEW)
│
└── presentation/                          # Presentation Layer (NEW STRUCTURE)
    ├── base/                              # Base classes for MVVM pattern
    │   ├── BaseViewModel.kt
    │   ├── BaseFragment.kt
    │   ├── UiState.kt
    │   └── ViewModelFactory.kt
    │
    ├── viewmodel/
    │   ├── MapViewModel.kt
    │   ├── MapCoordinatorViewModel.kt (shared for MainActivity)
    │   ├── OverlayViewModel.kt
    │   ├── LocationViewModel.kt
    │   ├── MeasureToolViewModel.kt
    │   ├── PoiListViewModel.kt
    │   ├── PoiDetailsViewModel.kt
    │   ├── TrackListViewModel.kt
    │   ├── TrackDetailsViewModel.kt
    │   ├── DownloadViewModel.kt
    │   ├── SettingsViewModel.kt
    │   └── (Other ViewModels)
    │
    ├── ui/
    │   ├── MainActivity.kt (Host for map fragments)
    │   ├── POI/
    │   │   ├── PoiListFragment.kt
    │   │   └── PoiDetailsFragment.kt
    │   ├── Track/
    │   │   ├── TrackListFragment.kt
    │   │   └── TrackDetailsFragment.kt
    │   ├── Map/
    │   │   ├── MapViewFragment.kt
    │   │   ├── OverlayControlFragment.kt
    │   │   ├── LocationTrackingFragment.kt
    │   │   ├── MeasureToolFragment.kt
    │   │   └── MapSourceSelectionFragment.kt
    │   ├── Download/
    │   │   ├── DownloadSelectionFragment.kt
    │   │   ├── AreaSelectionFragment.kt
    │   │   └── DownloadProgressFragment.kt
    │   └── Settings/
    │       └── SettingsFragment.kt
    │
    ├── state/                             # UI State classes
    │   ├── MapViewState.kt
    │   ├── OverlayState.kt
    │   ├── LocationState.kt
    │   ├── PoiListState.kt
    │   ├── TrackListState.kt
    │   └── (Other state classes)
    │
    └── adapter/                           # Adapters (updated for Flow/StateFlow)
        ├── PoiListAdapter.kt
        ├── TrackListAdapter.kt
        └── (Other adapters)
```

---

## KEY ARCHITECTURAL PATTERNS

### 1. State Management Pattern

```kotlin
// ViewModel State Declaration
sealed class UiState<out T> {
    object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val exception: Exception) : UiState<Nothing>()
    object Empty : UiState<Nothing>()
}

// ViewModel Implementation
class PoiListViewModel @Inject constructor(
    private val poiRepository: IPoiRepository,
    private val ioDispatcher: CoroutineDispatcher
) : BaseViewModel() {

    private val _poiListState = MutableStateFlow<UiState<List<PoiPoint>>>(UiState.Loading)
    val poiListState: StateFlow<UiState<List<PoiPoint>>> = _poiListState.asStateFlow()

    init {
        loadPois()
    }

    private fun loadPois() {
        viewModelScope.launch {
            try {
                poiRepository.getAllPois()
                    .collect { pois ->
                        _poiListState.value = if (pois.isEmpty()) {
                            UiState.Empty
                        } else {
                            UiState.Success(pois)
                        }
                    }
            } catch (e: Exception) {
                _poiListState.value = UiState.Error(e)
            }
        }
    }
}

// Fragment Usage
class PoiListFragment : BaseFragment<PoiListViewModel>() {
    private val viewModel: PoiListViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.poiListState.collect { state ->
                    when (state) {
                        is UiState.Loading -> showLoading()
                        is UiState.Success -> showPois(state.data)
                        is UiState.Error -> showError(state.exception)
                        is UiState.Empty -> showEmpty()
                    }
                }
            }
        }
    }
}
```

### 2. Shared ViewModel Pattern (For Parent-Child Fragment Communication)

```kotlin
// Shared ViewModel between MapViewFragment and OverlayControlFragment
class MapCoordinatorViewModel @Inject constructor(
    private val overlayRepository: IOverlayRepository
) : BaseViewModel() {

    private val _selectedOverlay = MutableStateFlow<String?>(null)
    val selectedOverlay: StateFlow<String?> = _selectedOverlay.asStateFlow()

    fun selectOverlay(overlayId: String) {
        _selectedOverlay.value = overlayId
    }
}

// Parent Fragment (MapViewFragment)
class MapViewFragment : BaseFragment() {
    private val coordinatorVM: MapCoordinatorViewModel by activityViewModels()
}

// Child Fragment (OverlayControlFragment)
class OverlayControlFragment : BaseFragment() {
    private val coordinatorVM: MapCoordinatorViewModel by activityViewModels()

    fun onOverlayClick(overlayId: String) {
        coordinatorVM.selectOverlay(overlayId)
    }
}
```

### 3. Navigation Component Integration

```kotlin
// nav_graph.xml
<navigation>
    <fragment android:id="@+id/poiListFragment" ...>
        <action android:id="@+id/action_poi_list_to_poi_details"
                app:destination="@id/poiDetailsFragment" />
    </fragment>
    <fragment android:id="@+id/poiDetailsFragment" ... />
</navigation>

// In Fragment
override fun onPoiSelected(poi: PoiPoint) {
    val action = PoiListFragmentDirections.actionPoiListToPoiDetails(poi.id)
    findNavController().navigate(action)
}
```

---

## MIGRATION CHECKLIST BY SCREEN

| Screen | Current | Target | Phase | Priority |
|--------|---------|--------|-------|----------|
| **POI List** | ListActivity (Loader) | PoiListFragment + ViewModel | 2 | High |
| **POI Details** | PoiActivity (Intent) | PoiDetailsFragment + ViewModel | 2 | High |
| **Track List** | TrackListActivity | TrackListFragment + ViewModel | 2 | High |
| **Track Details** | TrackActivity | TrackDetailsFragment + ViewModel | 2 | High |
| **Maps** | MainActivity (1,909 lines) | 5+ Fragments + ViewModel | 2 | Critical |
| **Overlays** | Integrated in MainActivity | OverlayControlFragment + ViewModel | 2 | High |
| **Location Tracking** | Integrated in MainActivity | LocationTrackingFragment + ViewModel | 2 | High |
| **Measure Tool** | Integrated in MainActivity | MeasureToolFragment + ViewModel | 2 | Medium |
| **Map Source** | Integrated in MainActivity | MapSourceSelectionFragment + ViewModel | 2 | Medium |
| **Downloader** | DownloaderActivity | DownloadSelectionFragment + ViewModel | 3 | Medium |
| **Area Selector** | AreaSelectorActivity | AreaSelectionFragment + ViewModel | 3 | Medium |
| **Settings** | MainPreferences | SettingsFragment + DataStore | 3 | Low |

---

## IMPLEMENTATION ORDER & REASONING

**Phase 1 (Foundation)**: 1-2 weeks
1. Hilt DI setup (enables everything else)
2. Base MVVM classes (ensures consistency)
3. Repository completion (data layer ready)
4. Domain models + interfaces

**Phase 2 (High-Value Screens)**: 3-4 weeks
1. **POI List** (start here - simple, tests foundational patterns)
2. **Track List** (similar pattern to POI)
3. **Maps** (complex but core feature, affects many systems)

**Phase 3 (Remaining + Cleanup)**: 5-6 weeks
1. Download screens
2. Settings
3. Code cleanup and legacy removal

---

## BENEFITS & OUTCOMES

**After Migration**:
- ✅ **Testability**: ViewModels are easily testable with mock repositories
- ✅ **State Management**: No more SharedPreferences bugs, centralized in ViewModels
- ✅ **Configuration Changes**: Automatic retention via ViewModel
- ✅ **Lifecycle Safety**: No memory leaks, proper Lifecycle handling
- ✅ **Reactive UI**: Flow/StateFlow updates UI automatically
- ✅ **Code Reusability**: ViewModels can be shared across fragments
- ✅ **Clear Separation**: Data layer completely independent from UI
- ✅ **Maintainability**: Each feature in isolated Fragment + ViewModel pair
- ✅ **Scalability**: Adding new features follows established patterns

---

## ROLLBACK STRATEGY

**Principle**: Each phase should be independently deployable and reversible.

### Phase-by-Phase Rollback

**Phase 0 (Data Migration)**:
- **Rollback trigger**: Migration fails or data corruption detected
- **Rollback procedure**:
  1. App detects migration failure in `LegacyDataMigrator`
  2. Sets `migration_complete = false` in SharedPreferences
  3. Continues using legacy SQLite databases
  4. User can retry migration from settings
- **Safety**: Legacy database files remain untouched during migration
- **Recovery time**: Immediate (next app restart)

**Phase 1 (MVVM Foundation)**:
- **Rollback trigger**: Hilt configuration issues, DI failures
- **Rollback procedure**:
  1. Remove `@HiltAndroidApp` annotation from `MapApplication`
  2. Comment out Hilt module registrations
  3. Revert to direct instantiation of repositories
- **Safety**: No UI changes in this phase, low user impact
- **Recovery time**: Single code revert, rebuild

**Phase 2 (Screen Migration)**:
- **Rollback trigger**: Critical bugs in migrated screens, performance regression
- **Rollback procedure** (per screen):
  1. Feature flag: `USE_LEGACY_POI_SCREEN = true`
  2. Routing logic switches back to legacy Activity
  3. Room repositories remain but unused
  4. SharedPreferences continue to work
- **Implementation**:
  ```kotlin
  // In navigation/routing logic
  fun openPoiList() {
      if (FeatureFlags.USE_LEGACY_POI_SCREEN) {
          // Old: start PoiListActivity
          startActivity(Intent(this, PoiListActivity::class.java))
      } else {
          // New: navigate to PoiListFragment
          findNavController().navigate(R.id.poiListFragment)
      }
  }
  ```
- **Safety**: Both old and new implementations coexist
- **Recovery time**: Feature flag toggle, no rebuild required

**Phase 3 (Cleanup)**:
- **Rollback trigger**: Critical production issues after legacy code removal
- **Rollback procedure**:
  1. Restore legacy Activity files from git
  2. Restore deleted legacy code
  3. Rebuild and redeploy
- **Safety**: Git history preserves all deleted code
- **Recovery time**: Hours (requires rebuild and deployment)

### Feature Flag Implementation

**Add Feature Flags Early** (during Phase 1):

```kotlin
object FeatureFlags {
    private const val PREFS_NAME = "feature_flags"

    // Migration feature flags
    const val USE_MVVM_POI_SCREEN = "use_mvvm_poi_screen"
    const val USE_MVVM_TRACK_SCREEN = "use_mvvm_track_screen"
    const val USE_MVVM_MAP_SCREEN = "use_mvvm_map_screen"
    const val USE_ROOM_DATABASE = "use_room_database"

    fun isEnabled(context: Context, flag: String, defaultValue: Boolean = false): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(flag, defaultValue)
    }

    fun setEnabled(context: Context, flag: String, enabled: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(flag, enabled).apply()
    }
}
```

**Gradual Rollout Pattern**:
1. Deploy with feature flag OFF (legacy code active)
2. Enable for internal testing (QA team validates)
3. Enable for 10% of users (canary deployment)
4. Monitor crash reports and performance metrics
5. Gradually increase to 50%, 100%
6. If issues arise, toggle flag to 0% instantly

### Emergency Rollback Checklist

- [ ] Feature flags implemented for each migrated screen
- [ ] Legacy code remains in codebase until Phase 3
- [ ] Database migration is reversible (Phase 0)
- [ ] Performance benchmarks established before migration
- [ ] Crash reporting configured to track migration issues
- [ ] Monitoring dashboard shows feature flag adoption rates
- [ ] Rollback procedure documented and tested
- [ ] Git tags mark each phase completion

---

## RISKS & MITIGATIONS

| Risk | Mitigation | Rollback Option |
|------|-----------|-----------------|
| **Large codebase complexity** | Phased approach, start with simple screens | Feature flags per screen |
| **Legacy Java-Kotlin mix** | Gradual migration, keep legacy working during transition | Both implementations coexist |
| **SharedPreferences dependencies** | Create wrapper, inject instead of direct access | Wrapper supports both old/new |
| **Map rendering issues** | Isolate MapView in Fragment, test separately | Feature flag for map screen |
| **Data consistency** | Ensure Room repositories are single source of truth | Dual-write pattern (Phase 0) |
| **Navigation complexity** | Use Navigation Component, test navigation flows | Legacy Intent navigation works |
| **Performance regression** | Benchmark before/after, profile ViewModel overhead | Feature flag rollback |
| **Memory leaks in ViewModels** | Use viewLifecycleOwner, test with LeakCanary | Immediate feature flag disable |
| **Data migration failure** | Test with production DB copies, add retry logic | Continue with legacy DB |
| **User data loss** | Backup before migration, validate after | Restore from backup |

---

## SUCCESS METRICS

- [ ] Zero architecture-related bugs in migrated screens
- [ ] All migrated screens have ViewModel tests
- [ ] Configuration changes don't lose UI state
- [ ] Startup time improvements (less SharedPreferences loading)
- [ ] Code coverage improves by 40%+ (from testable ViewModels)
