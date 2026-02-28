"""
timeline.py — Normalizes timeline event data into the timeline_events table.

Sources:
  - Seed data generated from well-known biblical chronology (public domain knowledge)
  - Can be augmented with Viz.Bible data when available

Output table: timeline_events
"""

from __future__ import annotations

import sqlite3
from pathlib import Path

# Seed timeline events based on widely accepted biblical chronology.
# Years are approximate. Negative values = BCE, positive = CE.
# Format: (title, description, year_start, year_end, era, global_verse_id)
SEED_EVENTS: list[tuple[str, str, int, int | None, str, int | None]] = [
    # Creation & Patriarchs
    ("Creation", "God creates the heavens and the earth", -4004, None, "Creation", 1001001),
    ("The Fall", "Adam and Eve disobey God in the Garden of Eden", -4004, None, "Creation", 1003006),
    ("The Flood", "God sends a worldwide flood; Noah builds the ark", -2348, None, "Creation", 1006017),
    ("Tower of Babel", "Humanity attempts to build a tower to heaven", -2242, None, "Creation", 1011001),
    ("Call of Abraham", "God calls Abram to leave Ur and go to Canaan", -2091, None, "Patriarchs", 1012001),
    ("Covenant with Abraham", "God establishes His covenant with Abraham", -2067, None, "Patriarchs", 1015001),
    ("Binding of Isaac", "Abraham is tested by God with the sacrifice of Isaac", -2054, None, "Patriarchs", 1022001),
    ("Jacob and Esau", "Birth of Jacob and Esau to Isaac and Rebekah", -2006, None, "Patriarchs", 1025019),
    ("Joseph sold into slavery", "Joseph's brothers sell him to traders going to Egypt", -1898, None, "Patriarchs", 1037028),
    ("Jacob's family moves to Egypt", "Israel settles in Egypt during the famine", -1876, None, "Patriarchs", 1046001),

    # Exodus & Conquest
    ("Birth of Moses", "Moses is born in Egypt and placed in the Nile", -1527, None, "Exodus", 2002001),
    ("The Burning Bush", "God appears to Moses in a burning bush", -1447, None, "Exodus", 2003001),
    ("The Ten Plagues", "God sends ten plagues upon Egypt", -1446, None, "Exodus", 2007014),
    ("The Exodus", "Israel leaves Egypt, crossing the Red Sea", -1446, None, "Exodus", 2014001),
    ("Giving of the Law at Sinai", "God gives the Ten Commandments to Moses", -1446, None, "Exodus", 2020001),
    ("40 Years in the Wilderness", "Israel wanders in the desert for 40 years", -1446, -1406, "Exodus", 4014033),
    ("Death of Moses", "Moses views the Promised Land and dies", -1406, None, "Exodus", 5034001),
    ("Conquest of Canaan", "Joshua leads Israel into the Promised Land", -1406, -1375, "Exodus", 6001001),
    ("Fall of Jericho", "The walls of Jericho fall before Israel", -1406, None, "Exodus", 6006020),

    # Judges
    ("Period of the Judges", "Cycles of apostasy, oppression, and deliverance", -1375, -1050, "Judges", 7002016),
    ("Deborah judges Israel", "Deborah leads Israel to victory over Sisera", -1209, None, "Judges", 7004004),
    ("Gideon defeats the Midianites", "Gideon's 300 men defeat the Midianite army", -1169, None, "Judges", 7007001),
    ("Samson", "Samson judges Israel for 20 years", -1075, -1055, "Judges", 7013024),
    ("Ruth and Boaz", "Ruth's loyalty and marriage to Boaz", -1100, None, "Judges", 8001001),
    ("Birth of Samuel", "Hannah dedicates Samuel to the Lord", -1105, None, "Judges", 9001020),

    # United Monarchy
    ("Saul anointed king", "Samuel anoints Saul as first king of Israel", -1050, None, "United Monarchy", 9010001),
    ("David anointed by Samuel", "God chooses David to replace Saul", -1025, None, "United Monarchy", 9016013),
    ("David defeats Goliath", "Young David kills the Philistine giant", -1024, None, "United Monarchy", 9017049),
    ("David becomes king", "David is crowned king over all Israel", -1010, None, "United Monarchy", 10005003),
    ("Solomon builds the Temple", "Construction of the First Temple in Jerusalem", -966, -959, "United Monarchy", 11006001),
    ("Dedication of the Temple", "Solomon dedicates the Temple to the Lord", -959, None, "United Monarchy", 11008001),
    ("Death of Solomon", "Solomon dies; the kingdom is divided", -931, None, "United Monarchy", 11011043),

    # Divided Monarchy
    ("Kingdom divided", "Rehoboam and Jeroboam split the kingdom", -931, None, "Divided Monarchy", 11012001),
    ("Elijah on Mount Carmel", "Elijah challenges the prophets of Baal", -869, None, "Divided Monarchy", 11018001),
    ("Fall of Samaria (Northern Kingdom)", "Assyria conquers the Northern Kingdom of Israel", -722, None, "Divided Monarchy", 12017006),
    ("Hezekiah's reforms", "King Hezekiah restores worship in Judah", -715, None, "Divided Monarchy", 12018001),
    ("Josiah's reforms", "King Josiah rediscovers the Law and reforms Judah", -622, None, "Divided Monarchy", 12022001),

    # Exile
    ("Fall of Jerusalem", "Babylon destroys Jerusalem and the Temple", -586, None, "Exile", 12025001),
    ("Babylonian Exile begins", "Judah is taken into captivity in Babylon", -586, -538, "Exile", 24052001),
    ("Daniel in Babylon", "Daniel serves in the Babylonian and Persian courts", -605, -536, "Exile", 27001001),
    ("Fiery furnace", "Shadrach, Meshach, and Abednego in the furnace", -586, None, "Exile", 27003001),
    ("Daniel in the lion's den", "Daniel is thrown into the den of lions", -539, None, "Exile", 27006001),

    # Return & Restoration
    ("Decree of Cyrus", "Cyrus the Great allows Jews to return to Jerusalem", -538, None, "Return", 15001001),
    ("Second Temple built", "Zerubbabel completes the rebuilding of the Temple", -516, None, "Return", 15006015),
    ("Ezra returns to Jerusalem", "Ezra leads a group back and restores the Law", -458, None, "Return", 15007001),
    ("Nehemiah rebuilds the walls", "Nehemiah leads the rebuilding of Jerusalem's walls", -445, None, "Return", 16002017),
    ("Esther saves her people", "Esther intervenes to prevent the destruction of the Jews", -473, None, "Return", 17004001),

    # Intertestamental
    ("Intertestamental period", "400 years between Malachi and the NT events", -430, -5, "Intertestamental", None),

    # New Testament
    ("Birth of Jesus", "Jesus is born in Bethlehem", -5, None, "New Testament", 42002007),
    ("Baptism of Jesus", "John baptizes Jesus in the Jordan River", 26, None, "New Testament", 40003013),
    ("Sermon on the Mount", "Jesus delivers the Sermon on the Mount", 27, None, "New Testament", 40005001),
    ("Feeding of the 5000", "Jesus feeds 5000 with five loaves and two fish", 29, None, "New Testament", 43006001),
    ("Transfiguration", "Jesus is transfigured on the mountain", 29, None, "New Testament", 40017001),
    ("Triumphal Entry", "Jesus enters Jerusalem on a donkey", 30, None, "New Testament", 40021001),
    ("The Last Supper", "Jesus shares the Passover meal with His disciples", 30, None, "New Testament", 40026017),
    ("Crucifixion of Jesus", "Jesus is crucified at Golgotha", 30, None, "New Testament", 40027035),
    ("Resurrection of Jesus", "Jesus rises from the dead on the third day", 30, None, "New Testament", 40028006),
    ("Ascension of Jesus", "Jesus ascends to heaven from the Mount of Olives", 30, None, "New Testament", 44001009),
    ("Day of Pentecost", "The Holy Spirit descends upon the disciples", 30, None, "New Testament", 44002001),

    # Early Church
    ("Conversion of Paul", "Saul encounters Jesus on the road to Damascus", 34, None, "Early Church", 44009001),
    ("Paul's first missionary journey", "Paul and Barnabas travel through Asia Minor", 46, 48, "Early Church", 44013001),
    ("Council of Jerusalem", "The apostles decide on Gentile inclusion", 49, None, "Early Church", 44015001),
    ("Paul's second missionary journey", "Paul travels through Greece", 49, 52, "Early Church", 44015040),
    ("Paul's third missionary journey", "Paul revisits churches in Asia Minor and Greece", 53, 57, "Early Church", 44018023),
    ("Paul arrested in Jerusalem", "Paul is arrested and appeals to Caesar", 57, None, "Early Church", 44021030),
    ("Paul in Rome", "Paul arrives in Rome under house arrest", 60, 62, "Early Church", 44028016),
    ("Destruction of the Temple", "Romans destroy the Second Temple in Jerusalem", 70, None, "Early Church", None),
    ("John writes Revelation", "The apostle John receives visions on Patmos", 95, None, "Early Church", 66001001),
]


def normalize(raw_dir: Path, db: sqlite3.Connection) -> None:
    """Insert seed timeline events."""
    print("    → Inserting timeline events")

    count = 0
    for title, description, year_start, year_end, era, global_verse_id in SEED_EVENTS:
        db.execute(
            """INSERT INTO timeline_events
               (title, description, year_start, year_end, era, global_verse_id)
               VALUES (?, ?, ?, ?, ?, ?)""",
            (title, description, year_start, year_end, era, global_verse_id),
        )
        count += 1

    db.commit()
    print(f"      ✓ Timeline events: {count}")
