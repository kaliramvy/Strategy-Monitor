# Strategy Tracker - Android Trading Journal App

A comprehensive Android trading journal app with **floating overlay buttons** for quick trade entry/exit logging while using other apps (trading platforms). Each strategy has independent tracking, statistics, and configurable P&L values.

## 📱 Features

### Home Page
- Grid display of strategy boxes with rounded corners
- Each box shows strategy title and overlay activation button
- FAB button to add new strategies
- Long-press to delete strategies

### Floating Overlay System
Three independently draggable circular buttons that float over other apps:
- **Blue (Entry)**: Records current DateTime as trade entry
- **Green (Profit Exit)**: Records exit DateTime, marks trade as WIN
- **Red (Loss Exit)**: Records exit DateTime, marks trade as LOSS

**Button State Logic:**
- IDLE: Blue enabled, Green/Red disabled (50% opacity)
- After Blue tap: Blue disabled, Green/Red enabled
- After Green/Red tap: Trade saved, reset to IDLE

### Strategy Detail Page
- **Description Box**: Multi-line text for strategy notes (2000 char limit)
- **Trade Settings**: Configure profit and loss amounts per trade
- **Statistics Overview**: Total Trades, Wins, Losses, Win Rate %, Total P&L
- **Charts**:
  - Win/Loss Pie Chart
  - Cumulative P&L Line Chart
  - Daily P&L Bar Chart (current month)
  - Monthly P&L Bar Chart (current year)
- **Trade Log Table**: Chronological list with entry/exit times, result, P&L
- **CSV Export**: Download trade history to share

## 🛠 Technical Stack

- **Language**: Kotlin
- **Architecture**: MVVM + Repository Pattern
- **Database**: Room Persistence Library
- **Charts**: MPAndroidChart
- **UI**: Material Design 3
- **Concurrency**: Kotlin Coroutines

## 📁 Project Structure

```
app/
├── data/
│   ├── database/       # Room Database, DAOs, Type Converters
│   ├── models/         # Strategy, Trade, OverlayPosition entities
│   └── repository/     # TradeRepository
├── ui/
│   ├── home/           # HomeActivity, ViewModel, Adapter
│   ├── detail/         # StrategyDetailActivity, ViewModel
│   └── dialogs/        # AddStrategyDialog
├── overlay/
│   ├── OverlayService  # Foreground Service for floating buttons
│   └── BootReceiver    # Restore overlay after device boot
└── utils/
    ├── CSVExporter     # Export trades to CSV
    └── DateTimeUtils   # Date/time formatting utilities
```

## 📋 Database Schema

### strategies
| Column | Type | Description |
|--------|------|-------------|
| id | Long (PK) | Auto-generated ID |
| name | String | Strategy name |
| description | String | Strategy notes |
| profitAmount | Double | Profit per winning trade |
| lossAmount | Double | Loss per losing trade |
| createdAt | Long | Creation timestamp |
| updatedAt | Long | Last update timestamp |

### trades
| Column | Type | Description |
|--------|------|-------------|
| id | Long (PK) | Auto-generated ID |
| strategyId | Long (FK) | Reference to strategy |
| entryDateTime | Long | Trade entry timestamp |
| exitDateTime | Long? | Trade exit timestamp |
| result | TradeResult? | WIN or LOSS |
| pnlAmount | Double? | Profit/Loss amount |
| isActive | Boolean | Is trade currently open |

### overlay_positions
| Column | Type | Description |
|--------|------|-------------|
| id | Long (PK) | Auto-generated ID |
| strategyId | Long (FK) | Reference to strategy |
| buttonType | ButtonType | BLUE, GREEN, or RED |
| positionX | Int | X position on screen |
| positionY | Int | Y position on screen |

## 🔐 Permissions Required

- `SYSTEM_ALERT_WINDOW` - Display overlay buttons over other apps
- `FOREGROUND_SERVICE` - Run overlay service in background
- `POST_NOTIFICATIONS` - Show notification when overlay is active
- `WRITE_EXTERNAL_STORAGE` (SDK < 29) - Save CSV exports

## 🎨 Color Palette

| Color | Hex | Usage |
|-------|-----|-------|
| Blue | #2196F3 | Primary, Entry button |
| Green | #4CAF50 | Profit, Win indicators |
| Red | #F44336 | Loss indicators |
| Background | #FAFAFA | Light background |
| Card | #FFFFFF | Card backgrounds |

## 🚀 Getting Started

1. Clone the repository
2. Open in Android Studio (Arctic Fox or later)
3. Sync Gradle dependencies
4. Run on device/emulator (API 26+)

## 📱 Minimum Requirements

- Android 8.0 (API 26) or higher
- Overlay permission must be granted manually in Settings

## 📄 CSV Export Format

```csv
Trade_No,Entry_DateTime,Exit_DateTime,Result,PnL_Amount
1,2024-01-15 10:30:00,2024-01-15 11:45:00,WIN,+100.00
2,2024-01-15 14:20:00,2024-01-15 15:10:00,LOSS,-50.00
```

## 📝 License

This project is open source and available under the MIT License.

## 🤝 Contributing

Contributions, issues, and feature requests are welcome!

## 📧 Contact

For questions or feedback, please open an issue on GitHub.
