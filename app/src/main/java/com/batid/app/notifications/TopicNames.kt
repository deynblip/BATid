package com.batid.app.notifications

import com.batid.app.data.model.Program
import com.batid.app.data.model.YearLevel

object TopicNames {

    fun topic(program: Program, year: YearLevel): String =
        "${program.name.lowercase()}_${year.topicKey}"

    /** Topics a student of [program] / [year] should be subscribed to. */
    fun subscriptionsFor(program: Program, year: YearLevel): Set<String> =
        setOf(topic(program, year), topic(program, YearLevel.ALL))


    /** Every possible topic; used to unsubscribe the old ones when a student changes program/year. */
    fun allTopics(): Set<String> =
        Program.entries.flatMap { p -> YearLevel.entries.map { y -> topic(p, y) } }.toSet()
}
