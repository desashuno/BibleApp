"""
parallels.py — Comprehensive Gospel harmony (synoptic parallels).

Based on standard academic gospel harmony categories. These correspondences
between Gospel passages are established scholarly facts, not copyrighted material.

Output table: parallel_passages
"""

from __future__ import annotations

import sqlite3
from pathlib import Path

# Each entry: (group_id, label, [(book_num, chapter, verse_start, verse_end)])
# Book numbers: Matthew=40, Mark=41, Luke=42, John=43
#
# Standard harmony based on A.T. Robertson / Kurt Aland categories.
# ~220 groups covering the full Gospel narrative.

GOSPEL_HARMONY: list[tuple[int, str, list[tuple[int, int, int, int]]]] = [
    # ═══════════════════════════════════════════════════════════════════
    # I. BIRTH AND CHILDHOOD
    # ═══════════════════════════════════════════════════════════════════
    (1, "Prologue of John", [(43, 1, 1, 18)]),
    (2, "Genealogy of Jesus", [(40, 1, 1, 17), (42, 3, 23, 38)]),
    (3, "Birth of John the Baptist foretold", [(42, 1, 5, 25)]),
    (4, "Annunciation to Mary", [(42, 1, 26, 38)]),
    (5, "Mary visits Elizabeth", [(42, 1, 39, 56)]),
    (6, "Birth of John the Baptist", [(42, 1, 57, 80)]),
    (7, "Angel appears to Joseph", [(40, 1, 18, 25)]),
    (8, "Birth of Jesus", [(40, 1, 25, 25), (42, 2, 1, 7)]),
    (9, "Shepherds visit", [(42, 2, 8, 20)]),
    (10, "Circumcision and presentation", [(42, 2, 21, 40)]),
    (11, "Visit of the Magi", [(40, 2, 1, 12)]),
    (12, "Flight to Egypt", [(40, 2, 13, 23)]),
    (13, "Boy Jesus in the Temple", [(42, 2, 41, 52)]),

    # ═══════════════════════════════════════════════════════════════════
    # II. PREPARATION FOR MINISTRY
    # ═══════════════════════════════════════════════════════════════════
    (14, "John the Baptist's ministry", [
        (40, 3, 1, 12), (41, 1, 1, 8), (42, 3, 1, 18), (43, 1, 19, 28)]),
    (15, "Baptism of Jesus", [
        (40, 3, 13, 17), (41, 1, 9, 11), (42, 3, 21, 22)]),
    (16, "Temptation of Jesus", [
        (40, 4, 1, 11), (41, 1, 12, 13), (42, 4, 1, 13)]),
    (17, "Testimony of John the Baptist", [(43, 1, 29, 34)]),
    (18, "First disciples called", [(43, 1, 35, 51)]),
    (19, "Wedding at Cana", [(43, 2, 1, 12)]),

    # ═══════════════════════════════════════════════════════════════════
    # III. EARLY GALILEAN MINISTRY
    # ═══════════════════════════════════════════════════════════════════
    (20, "First cleansing of the Temple", [(43, 2, 13, 25)]),
    (21, "Nicodemus visits Jesus", [(43, 3, 1, 21)]),
    (22, "John's final testimony", [(43, 3, 22, 36)]),
    (23, "Woman at the well", [(43, 4, 1, 42)]),
    (24, "Jesus begins Galilean ministry", [
        (40, 4, 12, 17), (41, 1, 14, 15), (42, 4, 14, 15)]),
    (25, "Rejection at Nazareth", [(42, 4, 16, 30)]),
    (26, "Calling of the first disciples", [
        (40, 4, 18, 22), (41, 1, 16, 20), (42, 5, 1, 11)]),
    (27, "Healing in Capernaum synagogue", [(41, 1, 21, 28), (42, 4, 31, 37)]),
    (28, "Healing of Peter's mother-in-law", [
        (40, 8, 14, 15), (41, 1, 29, 31), (42, 4, 38, 39)]),
    (29, "Healing many at evening", [
        (40, 8, 16, 17), (41, 1, 32, 34), (42, 4, 40, 41)]),
    (30, "Preaching tour in Galilee", [
        (40, 4, 23, 25), (41, 1, 35, 39), (42, 4, 42, 44)]),
    (31, "Healing a leper", [
        (40, 8, 1, 4), (41, 1, 40, 45), (42, 5, 12, 16)]),
    (32, "Healing a paralytic", [
        (40, 9, 1, 8), (41, 2, 1, 12), (42, 5, 17, 26)]),
    (33, "Calling of Matthew/Levi", [
        (40, 9, 9, 13), (41, 2, 13, 17), (42, 5, 27, 32)]),
    (34, "Question about fasting", [
        (40, 9, 14, 17), (41, 2, 18, 22), (42, 5, 33, 39)]),
    (35, "Healing at pool of Bethesda", [(43, 5, 1, 47)]),
    (36, "Plucking grain on the Sabbath", [
        (40, 12, 1, 8), (41, 2, 23, 28), (42, 6, 1, 5)]),
    (37, "Healing on the Sabbath (withered hand)", [
        (40, 12, 9, 14), (41, 3, 1, 6), (42, 6, 6, 11)]),
    (38, "Crowds follow Jesus", [
        (40, 12, 15, 21), (41, 3, 7, 12)]),
    (39, "Choosing the Twelve", [
        (40, 10, 1, 4), (41, 3, 13, 19), (42, 6, 12, 16)]),

    # ═══════════════════════════════════════════════════════════════════
    # IV. SERMON ON THE MOUNT / PLAIN
    # ═══════════════════════════════════════════════════════════════════
    (40, "Beatitudes", [(40, 5, 1, 12), (42, 6, 20, 26)]),
    (41, "Salt and light", [(40, 5, 13, 16)]),
    (42, "Fulfillment of the Law", [(40, 5, 17, 20)]),
    (43, "Teaching on anger", [(40, 5, 21, 26)]),
    (44, "Teaching on adultery", [(40, 5, 27, 30)]),
    (45, "Teaching on divorce", [(40, 5, 31, 32)]),
    (46, "Teaching on oaths", [(40, 5, 33, 37)]),
    (47, "Teaching on retaliation", [(40, 5, 38, 42), (42, 6, 29, 30)]),
    (48, "Love your enemies", [(40, 5, 43, 48), (42, 6, 27, 36)]),
    (49, "Teaching on giving", [(40, 6, 1, 4)]),
    (50, "The Lord's Prayer", [(40, 6, 5, 15), (42, 11, 1, 4)]),
    (51, "Teaching on fasting", [(40, 6, 16, 18)]),
    (52, "Treasures in heaven", [(40, 6, 19, 24), (42, 12, 33, 34)]),
    (53, "Do not worry", [(40, 6, 25, 34), (42, 12, 22, 31)]),
    (54, "Judging others", [(40, 7, 1, 6), (42, 6, 37, 42)]),
    (55, "Ask, seek, knock", [(40, 7, 7, 12), (42, 11, 9, 13)]),
    (56, "The Golden Rule", [(40, 7, 12, 12), (42, 6, 31, 31)]),
    (57, "The narrow gate", [(40, 7, 13, 14), (42, 13, 24, 24)]),
    (58, "Tree and its fruit", [(40, 7, 15, 20), (42, 6, 43, 45)]),
    (59, "Wise and foolish builders", [(40, 7, 24, 29), (42, 6, 46, 49)]),

    # ═══════════════════════════════════════════════════════════════════
    # V. CONTINUED GALILEAN MINISTRY
    # ═══════════════════════════════════════════════════════════════════
    (60, "Centurion's servant healed", [(40, 8, 5, 13), (42, 7, 1, 10)]),
    (61, "Widow's son at Nain raised", [(42, 7, 11, 17)]),
    (62, "John's question from prison", [(40, 11, 2, 19), (42, 7, 18, 35)]),
    (63, "Woes on unrepentant cities", [(40, 11, 20, 24)]),
    (64, "Rest for the weary", [(40, 11, 25, 30)]),
    (65, "Woman anoints Jesus (Galilee)", [(42, 7, 36, 50)]),
    (66, "Women who supported Jesus", [(42, 8, 1, 3)]),
    (67, "Blasphemy against the Spirit", [
        (40, 12, 22, 37), (41, 3, 20, 30)]),
    (68, "Sign of Jonah", [(40, 12, 38, 45), (42, 11, 29, 32)]),
    (69, "Jesus' true family", [
        (40, 12, 46, 50), (41, 3, 31, 35), (42, 8, 19, 21)]),

    # ═══════════════════════════════════════════════════════════════════
    # VI. PARABLES
    # ═══════════════════════════════════════════════════════════════════
    (70, "Parable of the Sower", [
        (40, 13, 1, 23), (41, 4, 1, 20), (42, 8, 4, 15)]),
    (71, "Lamp under a basket", [(41, 4, 21, 25), (42, 8, 16, 18)]),
    (72, "Parable of the growing seed", [(41, 4, 26, 29)]),
    (73, "Parable of the weeds", [(40, 13, 24, 30)]),
    (74, "Parable of the mustard seed", [
        (40, 13, 31, 32), (41, 4, 30, 32), (42, 13, 18, 19)]),
    (75, "Parable of the leaven", [(40, 13, 33, 33), (42, 13, 20, 21)]),
    (76, "Explanation of the weeds", [(40, 13, 36, 43)]),
    (77, "Hidden treasure and pearl", [(40, 13, 44, 46)]),
    (78, "Parable of the net", [(40, 13, 47, 52)]),

    # ═══════════════════════════════════════════════════════════════════
    # VII. MIRACLES AND TEACHING
    # ═══════════════════════════════════════════════════════════════════
    (79, "Calming the storm", [
        (40, 8, 23, 27), (41, 4, 35, 41), (42, 8, 22, 25)]),
    (80, "Gerasene demoniac", [
        (40, 8, 28, 34), (41, 5, 1, 20), (42, 8, 26, 39)]),
    (81, "Jairus' daughter and bleeding woman", [
        (40, 9, 18, 26), (41, 5, 21, 43), (42, 8, 40, 56)]),
    (82, "Rejection at Nazareth (second)", [
        (40, 13, 53, 58), (41, 6, 1, 6)]),
    (83, "Sending out the Twelve", [
        (40, 10, 5, 42), (41, 6, 7, 13), (42, 9, 1, 6)]),
    (84, "Death of John the Baptist", [
        (40, 14, 1, 12), (41, 6, 14, 29), (42, 9, 7, 9)]),
    (85, "Feeding of the 5000", [
        (40, 14, 13, 21), (41, 6, 30, 44), (42, 9, 10, 17), (43, 6, 1, 15)]),
    (86, "Walking on water", [
        (40, 14, 22, 33), (41, 6, 45, 52), (43, 6, 16, 21)]),
    (87, "Healings at Gennesaret", [
        (40, 14, 34, 36), (41, 6, 53, 56)]),
    (88, "Bread of Life discourse", [(43, 6, 22, 71)]),
    (89, "Clean and unclean", [
        (40, 15, 1, 20), (41, 7, 1, 23)]),
    (90, "Syrophoenician woman's faith", [
        (40, 15, 21, 28), (41, 7, 24, 30)]),
    (91, "Healing of deaf-mute", [(41, 7, 31, 37)]),
    (92, "Feeding of the 4000", [
        (40, 15, 32, 39), (41, 8, 1, 10)]),
    (93, "Pharisees demand a sign", [
        (40, 16, 1, 4), (41, 8, 11, 13)]),
    (94, "Leaven of the Pharisees", [
        (40, 16, 5, 12), (41, 8, 14, 21)]),
    (95, "Healing of blind man at Bethsaida", [(41, 8, 22, 26)]),

    # ═══════════════════════════════════════════════════════════════════
    # VIII. PETER'S CONFESSION AND TRANSFIGURATION
    # ═══════════════════════════════════════════════════════════════════
    (96, "Peter's confession", [
        (40, 16, 13, 20), (41, 8, 27, 30), (42, 9, 18, 21)]),
    (97, "First prediction of the Passion", [
        (40, 16, 21, 28), (41, 8, 31, 38), (42, 9, 22, 27)]),
    (98, "Transfiguration", [
        (40, 17, 1, 13), (41, 9, 2, 13), (42, 9, 28, 36)]),
    (99, "Healing of epileptic boy", [
        (40, 17, 14, 21), (41, 9, 14, 29), (42, 9, 37, 43)]),
    (100, "Second prediction of the Passion", [
        (40, 17, 22, 23), (41, 9, 30, 32), (42, 9, 43, 45)]),
    (101, "Temple tax", [(40, 17, 24, 27)]),
    (102, "Who is the greatest?", [
        (40, 18, 1, 5), (41, 9, 33, 37), (42, 9, 46, 48)]),
    (103, "Unknown exorcist", [(41, 9, 38, 41), (42, 9, 49, 50)]),
    (104, "Warnings about causing sin", [
        (40, 18, 6, 9), (41, 9, 42, 50)]),
    (105, "Parable of the lost sheep", [(40, 18, 10, 14), (42, 15, 1, 7)]),
    (106, "Discipline in the church", [(40, 18, 15, 20)]),
    (107, "Parable of the unforgiving servant", [(40, 18, 21, 35)]),

    # ═══════════════════════════════════════════════════════════════════
    # IX. JOURNEY TO JERUSALEM (Luke's travel narrative)
    # ═══════════════════════════════════════════════════════════════════
    (108, "Samaritan rejection", [(42, 9, 51, 56)]),
    (109, "Cost of following Jesus", [
        (40, 8, 19, 22), (42, 9, 57, 62)]),
    (110, "Sending of the Seventy-two", [(42, 10, 1, 24)]),
    (111, "Parable of the Good Samaritan", [(42, 10, 25, 37)]),
    (112, "Mary and Martha", [(42, 10, 38, 42)]),
    (113, "Parable of the friend at midnight", [(42, 11, 5, 13)]),
    (114, "Beelzebul controversy (Luke)", [(42, 11, 14, 28)]),
    (115, "Lamp of the body", [(42, 11, 33, 36)]),
    (116, "Woes on Pharisees and scribes", [(42, 11, 37, 54)]),
    (117, "Warnings and encouragements", [(42, 12, 1, 12)]),
    (118, "Parable of the rich fool", [(42, 12, 13, 21)]),
    (119, "Watchful servants", [(42, 12, 35, 48)]),
    (120, "Division, not peace", [(40, 10, 34, 39), (42, 12, 49, 53)]),
    (121, "Interpreting the times", [(42, 12, 54, 59)]),
    (122, "Repent or perish", [(42, 13, 1, 9)]),
    (123, "Healing a crippled woman on Sabbath", [(42, 13, 10, 17)]),
    (124, "The narrow door", [(42, 13, 22, 30)]),
    (125, "Lament over Jerusalem", [
        (40, 23, 37, 39), (42, 13, 31, 35)]),
    (126, "Healing on the Sabbath (dropsy)", [(42, 14, 1, 6)]),
    (127, "Teaching on humility", [(42, 14, 7, 14)]),
    (128, "Parable of the great banquet", [(42, 14, 15, 24)]),
    (129, "Cost of discipleship", [(42, 14, 25, 35)]),
    (130, "Parable of the lost coin", [(42, 15, 8, 10)]),
    (131, "Parable of the prodigal son", [(42, 15, 11, 32)]),
    (132, "Parable of the shrewd manager", [(42, 16, 1, 13)]),
    (133, "The rich man and Lazarus", [(42, 16, 19, 31)]),
    (134, "Teaching on forgiveness and faith", [(42, 17, 1, 10)]),
    (135, "Ten lepers healed", [(42, 17, 11, 19)]),
    (136, "Coming of the Kingdom", [(42, 17, 20, 37)]),
    (137, "Parable of the persistent widow", [(42, 18, 1, 8)]),
    (138, "Parable of the Pharisee and tax collector", [(42, 18, 9, 14)]),

    # ═══════════════════════════════════════════════════════════════════
    # X. LATER JUDEAN/PEREAN MINISTRY
    # ═══════════════════════════════════════════════════════════════════
    (139, "Feast of Tabernacles", [(43, 7, 1, 52)]),
    (140, "Woman caught in adultery", [(43, 8, 1, 11)]),
    (141, "Light of the World", [(43, 8, 12, 59)]),
    (142, "Healing the man born blind", [(43, 9, 1, 41)]),
    (143, "Good Shepherd discourse", [(43, 10, 1, 21)]),
    (144, "Feast of Dedication", [(43, 10, 22, 42)]),
    (145, "Teaching on divorce", [
        (40, 19, 1, 12), (41, 10, 1, 12)]),
    (146, "Blessing the children", [
        (40, 19, 13, 15), (41, 10, 13, 16), (42, 18, 15, 17)]),
    (147, "Rich young ruler", [
        (40, 19, 16, 30), (41, 10, 17, 31), (42, 18, 18, 30)]),
    (148, "Parable of the workers in the vineyard", [(40, 20, 1, 16)]),
    (149, "Third prediction of the Passion", [
        (40, 20, 17, 19), (41, 10, 32, 34), (42, 18, 31, 34)]),
    (150, "Request of James and John", [
        (40, 20, 20, 28), (41, 10, 35, 45)]),
    (151, "Healing of blind Bartimaeus", [
        (40, 20, 29, 34), (41, 10, 46, 52), (42, 18, 35, 43)]),
    (152, "Zacchaeus", [(42, 19, 1, 10)]),
    (153, "Parable of the ten minas", [(42, 19, 11, 27)]),
    (154, "Raising of Lazarus", [(43, 11, 1, 44)]),
    (155, "Plot to kill Jesus", [(43, 11, 45, 57)]),
    (156, "Anointing at Bethany", [
        (40, 26, 6, 13), (41, 14, 3, 9), (43, 12, 1, 8)]),

    # ═══════════════════════════════════════════════════════════════════
    # XI. PASSION WEEK
    # ═══════════════════════════════════════════════════════════════════
    (157, "Triumphal Entry", [
        (40, 21, 1, 11), (41, 11, 1, 11), (42, 19, 28, 44), (43, 12, 12, 19)]),
    (158, "Cursing the fig tree", [
        (40, 21, 18, 22), (41, 11, 12, 14)]),
    (159, "Cleansing the Temple", [
        (40, 21, 12, 17), (41, 11, 15, 19), (42, 19, 45, 48)]),
    (160, "Lesson from the fig tree", [(41, 11, 20, 26)]),
    (161, "Authority of Jesus questioned", [
        (40, 21, 23, 27), (41, 11, 27, 33), (42, 20, 1, 8)]),
    (162, "Parable of the two sons", [(40, 21, 28, 32)]),
    (163, "Parable of the wicked tenants", [
        (40, 21, 33, 46), (41, 12, 1, 12), (42, 20, 9, 19)]),
    (164, "Parable of the wedding banquet", [(40, 22, 1, 14)]),
    (165, "Paying taxes to Caesar", [
        (40, 22, 15, 22), (41, 12, 13, 17), (42, 20, 20, 26)]),
    (166, "Marriage at the resurrection", [
        (40, 22, 23, 33), (41, 12, 18, 27), (42, 20, 27, 40)]),
    (167, "Greatest commandment", [
        (40, 22, 34, 40), (41, 12, 28, 34)]),
    (168, "David's son and Lord", [
        (40, 22, 41, 46), (41, 12, 35, 37), (42, 20, 41, 44)]),
    (169, "Woes against the scribes and Pharisees", [
        (40, 23, 1, 36), (41, 12, 38, 40), (42, 20, 45, 47)]),
    (170, "Widow's offering", [(41, 12, 41, 44), (42, 21, 1, 4)]),
    (171, "Greeks seek Jesus", [(43, 12, 20, 36)]),
    (172, "Unbelief of the people", [(43, 12, 37, 50)]),

    # ═══════════════════════════════════════════════════════════════════
    # XII. OLIVET DISCOURSE
    # ═══════════════════════════════════════════════════════════════════
    (173, "Destruction of the Temple predicted", [
        (40, 24, 1, 2), (41, 13, 1, 2), (42, 21, 5, 6)]),
    (174, "Signs of the end", [
        (40, 24, 3, 14), (41, 13, 3, 13), (42, 21, 7, 19)]),
    (175, "Abomination of desolation", [
        (40, 24, 15, 28), (41, 13, 14, 23), (42, 21, 20, 24)]),
    (176, "Coming of the Son of Man", [
        (40, 24, 29, 31), (41, 13, 24, 27), (42, 21, 25, 28)]),
    (177, "Lesson of the fig tree (eschatological)", [
        (40, 24, 32, 35), (41, 13, 28, 31), (42, 21, 29, 33)]),
    (178, "Day and hour unknown", [
        (40, 24, 36, 44), (41, 13, 32, 37)]),
    (179, "Parable of the faithful servant", [
        (40, 24, 45, 51), (42, 12, 41, 48)]),
    (180, "Parable of the ten virgins", [(40, 25, 1, 13)]),
    (181, "Parable of the talents", [(40, 25, 14, 30)]),
    (182, "Sheep and goats", [(40, 25, 31, 46)]),

    # ═══════════════════════════════════════════════════════════════════
    # XIII. LAST SUPPER AND FAREWELL
    # ═══════════════════════════════════════════════════════════════════
    (183, "Plot to kill Jesus", [
        (40, 26, 1, 5), (41, 14, 1, 2), (42, 22, 1, 2)]),
    (184, "Judas agrees to betray", [
        (40, 26, 14, 16), (41, 14, 10, 11), (42, 22, 3, 6)]),
    (185, "Preparation for the Passover", [
        (40, 26, 17, 19), (41, 14, 12, 16), (42, 22, 7, 13)]),
    (186, "The Last Supper", [
        (40, 26, 20, 30), (41, 14, 17, 26), (42, 22, 14, 23)]),
    (187, "Washing of the disciples' feet", [(43, 13, 1, 20)]),
    (188, "Prediction of betrayal", [
        (40, 26, 21, 25), (41, 14, 18, 21), (42, 22, 21, 23), (43, 13, 21, 30)]),
    (189, "New commandment", [(43, 13, 31, 35)]),
    (190, "Prediction of Peter's denial", [
        (40, 26, 31, 35), (41, 14, 27, 31), (42, 22, 31, 38), (43, 13, 36, 38)]),
    (191, "Farewell discourse: the way, truth, life", [(43, 14, 1, 14)]),
    (192, "Promise of the Holy Spirit", [(43, 14, 15, 31)]),
    (193, "Vine and branches", [(43, 15, 1, 17)]),
    (194, "World's hatred", [(43, 15, 18, 27)]),
    (195, "Work of the Holy Spirit", [(43, 16, 1, 15)]),
    (196, "Sorrow turned to joy", [(43, 16, 16, 33)]),
    (197, "High Priestly Prayer", [(43, 17, 1, 26)]),

    # ═══════════════════════════════════════════════════════════════════
    # XIV. ARREST, TRIAL, AND CRUCIFIXION
    # ═══════════════════════════════════════════════════════════════════
    (198, "Gethsemane", [
        (40, 26, 36, 46), (41, 14, 32, 42), (42, 22, 39, 46), (43, 18, 1, 1)]),
    (199, "Arrest of Jesus", [
        (40, 26, 47, 56), (41, 14, 43, 52), (42, 22, 47, 53), (43, 18, 2, 12)]),
    (200, "Trial before the Sanhedrin", [
        (40, 26, 57, 68), (41, 14, 53, 65), (42, 22, 54, 71)]),
    (201, "Peter's denial", [
        (40, 26, 69, 75), (41, 14, 66, 72), (42, 22, 54, 62), (43, 18, 15, 27)]),
    (202, "Jesus before Pilate", [
        (40, 27, 1, 2), (41, 15, 1, 5), (42, 23, 1, 5), (43, 18, 28, 38)]),
    (203, "Jesus before Herod", [(42, 23, 6, 12)]),
    (204, "Pilate's verdict", [
        (40, 27, 15, 26), (41, 15, 6, 15), (42, 23, 13, 25), (43, 18, 39, 40)]),
    (205, "Death of Judas", [(40, 27, 3, 10)]),
    (206, "Soldiers mock Jesus", [
        (40, 27, 27, 31), (41, 15, 16, 20), (43, 19, 1, 3)]),
    (207, "Road to Golgotha", [
        (40, 27, 32, 34), (41, 15, 21, 22), (42, 23, 26, 32), (43, 19, 17, 17)]),
    (208, "Crucifixion", [
        (40, 27, 35, 44), (41, 15, 23, 32), (42, 23, 33, 43), (43, 19, 18, 27)]),
    (209, "Death of Jesus", [
        (40, 27, 45, 56), (41, 15, 33, 41), (42, 23, 44, 49), (43, 19, 28, 37)]),
    (210, "Burial of Jesus", [
        (40, 27, 57, 66), (41, 15, 42, 47), (42, 23, 50, 56), (43, 19, 38, 42)]),

    # ═══════════════════════════════════════════════════════════════════
    # XV. RESURRECTION AND APPEARANCES
    # ═══════════════════════════════════════════════════════════════════
    (211, "Empty tomb", [
        (40, 28, 1, 10), (41, 16, 1, 8), (42, 24, 1, 12), (43, 20, 1, 10)]),
    (212, "Appearance to Mary Magdalene", [
        (41, 16, 9, 11), (43, 20, 11, 18)]),
    (213, "Report of the guards", [(40, 28, 11, 15)]),
    (214, "Road to Emmaus", [(41, 16, 12, 13), (42, 24, 13, 35)]),
    (215, "Appearance to the disciples", [
        (41, 16, 14, 14), (42, 24, 36, 43), (43, 20, 19, 23)]),
    (216, "Thomas doubts and believes", [(43, 20, 24, 29)]),
    (217, "Appearance at the Sea of Galilee", [(43, 21, 1, 25)]),
    (218, "Great Commission", [
        (40, 28, 16, 20), (41, 16, 15, 18)]),
    (219, "Ascension", [
        (41, 16, 19, 20), (42, 24, 50, 53)]),
]


def normalize(raw_dir: Path, db: sqlite3.Connection) -> None:
    """Insert comprehensive Gospel harmony parallel passages."""
    print("    → Inserting Gospel harmony parallel passages")

    count = 0
    for group_id, label, passages in GOSPEL_HARMONY:
        for book_num, chapter, v_start, v_end in passages:
            for v in range(v_start, v_end + 1):
                global_id = book_num * 1_000_000 + chapter * 1_000 + v
                db.execute(
                    "INSERT INTO parallel_passages (group_id, global_verse_id, label) VALUES (?, ?, ?)",
                    (group_id, global_id, label),
                )
                count += 1

    db.commit()
    print(f"      ✓ Parallel passages: {count:,} entries in {len(GOSPEL_HARMONY)} groups")
