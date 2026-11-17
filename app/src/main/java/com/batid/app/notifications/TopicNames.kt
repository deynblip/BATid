package com.batid.app.notifications

import com.batid.app.data.model.Program
import com.batid.app.data.model.YearLevel

/**
 * FCM topic naming. Topic = "<program>_<year>", e.g. "bscs_3" or "bsit_all".
 *
 * - A student subscribes to their own year topic AND their program's "all years" topic.
 * - An announcement is sent to one topic per selected program (BSCS and/or BSIT)
 *   for the selected year level.
 */
object TopicNames {

    fun topic(program: Program, year: YearLevel): String =
        "${program.name.lowercase()}_${year.topicKey}"

    /** Topics a student of [program] / [year] should be subscribed to. */
    fun subscriptionsFor(program: Program, year: YearLevel): Set<String> =
        setOf(topic(program, year), topic(program, YearLevel.ALL))

    /** Topics an announcement should be sent to. */
    fun targetsFor(programs: Collection<Program>, year: YearLevel): Set<String> =
        programs.map { topic(it, year) }.toSet()

    /** Every possible topic; used to unsubscribe the old ones when a student changes program/year. */
    fun allTopics(): Set<String> =
        Program.entries.flatMap { p -> YearLevel.entries.map { y -> topic(p, y) } }.toSet()
}
