Smart Pantry Manager

A lightweight, offline-first Android application developed for MobileAppDev700.

Smart Pantry Manager solves the problem of kitchen inventory tracking by bridging the gap between what you actually have in stock and what you can realistically cook. It utilizes a zero-assumption strict-matching SQL algorithm to only suggest recipes when 100% of the required ingredients are currently available in your local database.

Key Features

Inventory Management (CRUD): Add, edit, delete, and track pantry ingredients.

Dynamic Expiry Tracking: Automatically calculates and color-codes item freshness (Fresh, Expiring Soon, Expired) using real-time date mathematics.

Strict-Matching Recipe Engine: Suggests meals based strictly on available inventory—if you are missing a single gram of an ingredient, the recipe is flagged.

Offline-First: Fully functional without an internet connection, utilizing an embedded relational database.

Technical Architecture

Language: Java

Database: Native Android SQLiteOpenHelper (Relational schema with Foreign Keys and ON DELETE CASCADE)

UI/UX: XML-based layouts, RecyclerView with ViewHolder pattern for flat memory allocation, and a custom high-contrast "Culinary Amber" OLED dark theme.

State Management: Pure Activity-based architecture (no Fragments) utilizing explicit Intents and FLAG_ACTIVITY_REORDER_TO_FRONT for memory-efficient lifecycle routing.
