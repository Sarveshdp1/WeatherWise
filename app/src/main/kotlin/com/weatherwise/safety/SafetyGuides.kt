package com.weatherwise.safety

import com.weatherwise.data.AlertType

// One offline safety guide: what to do BEFORE, DURING and AFTER a hazard.
data class SafetyGuide(
    val title: String,
    val emoji: String,
    val summary: String,
    val before: List<String>,
    val during: List<String>,
    val after: List<String>
)

/**
 * All safety guides live inside the app (no internet needed).
 * To add a new guide, just add another SafetyGuide(...) to the list below.
 */
object SafetyGuides {

    val all: List<SafetyGuide> = listOf(
        SafetyGuide(
            title = "Flood",
            emoji = "\uD83C\uDF0A",
            summary = "Heavy rain, rising rivers, waterlogging",
            before = listOf(
                "Know your area's flood risk and the nearest higher ground or shelter.",
                "Keep a go-bag ready: torch, power bank, medicines, drinking water, dry food, important documents in a waterproof cover.",
                "Move valuables and electronics to upper floors.",
                "Save emergency numbers on your phone."
            ),
            during = listOf(
                "Move to higher ground immediately if water is rising. Do not wait for instructions.",
                "Never walk, swim or drive through floodwater. Even shallow moving water can sweep you away.",
                "Switch off electricity and gas at the mains if it is safe to do so.",
                "Stay away from fallen power lines and do not touch electrical items when wet.",
                "Listen to radio or official alerts for updates."
            ),
            after = listOf(
                "Return home only when authorities say it is safe.",
                "Drink only boiled or bottled water.",
                "Throw away food that touched floodwater.",
                "Take photos of damage for insurance and be careful of snakes and weak walls."
            )
        ),
        SafetyGuide(
            title = "Earthquake",
            emoji = "\uD83C\uDFDA\uFE0F",
            summary = "Sudden ground shaking",
            before = listOf(
                "Secure heavy furniture, shelves and water heaters to walls.",
                "Identify safe spots: under a strong table, away from windows and glass.",
                "Keep a go-bag with torch, whistle, first-aid kit and water.",
                "Learn how to switch off gas and electricity."
            ),
            during = listOf(
                "DROP to the ground, take COVER under a sturdy table, HOLD ON and protect your head and neck.",
                "Stay away from windows, mirrors and heavy furniture.",
                "Do not run outside and do not use lifts.",
                "If outdoors, move to an open area away from buildings, trees and power lines.",
                "If driving, stop in an open area away from bridges and flyovers, and stay inside the vehicle."
            ),
            after = listOf(
                "Expect aftershocks. Drop, Cover and Hold On again each time.",
                "Check yourself and others for injuries and give first aid.",
                "If you smell gas, open windows, switch off the supply and leave the building.",
                "Send SMS instead of calling to keep phone lines free.",
                "Stay out of damaged buildings. If near the coast and the shaking was strong, move to higher ground."
            )
        ),
        SafetyGuide(
            title = "Cyclone & Strong Winds",
            emoji = "\uD83C\uDF2A\uFE0F",
            summary = "Storms, gale-force winds, cyclones",
            before = listOf(
                "Secure or bring in loose objects such as pots, boards and roof sheets.",
                "Charge phones and power banks. Store water, dry food and medicines for at least 3 days.",
                "Know your evacuation route and the nearest cyclone shelter.",
                "Follow official cyclone warnings."
            ),
            during = listOf(
                "Stay indoors, away from windows and glass doors, in a strong interior room.",
                "Do not go outside when it suddenly becomes calm. It may be the eye of the cyclone and strong winds will return.",
                "Switch off electricity and gas if there is flooding or damage.",
                "If told to evacuate, leave immediately."
            ),
            after = listOf(
                "Stay away from fallen power lines and floodwater.",
                "Do not enter damaged buildings.",
                "Use a torch instead of candles in case of gas leaks.",
                "Boil drinking water and check on elderly neighbours."
            )
        ),
        SafetyGuide(
            title = "Heatwave",
            emoji = "\u2600\uFE0F",
            summary = "Dangerously high temperatures",
            before = listOf(
                "Plan outdoor work for early morning or evening.",
                "Keep water and ORS at home.",
                "Use curtains or shades to keep rooms cool."
            ),
            during = listOf(
                "Drink water often, even if you are not thirsty. ORS, lemon water and buttermilk help.",
                "Avoid going out between 12 PM and 4 PM.",
                "Wear light, loose, light-coloured cotton clothes and cover your head.",
                "Never leave children or pets in a parked vehicle.",
                "Heat stroke signs: very high body temperature, hot dry skin, confusion, fainting. Call 108 or 112 and cool the person with wet cloths while waiting."
            ),
            after = listOf(
                "Rest and keep drinking fluids.",
                "Check on elderly people and neighbours.",
                "See a doctor if dizziness or nausea continues."
            )
        ),
        SafetyGuide(
            title = "Cold Wave",
            emoji = "\u2744\uFE0F",
            summary = "Very low temperatures, frost",
            before = listOf(
                "Keep warm clothes, blankets and extra food ready.",
                "Check that heaters and fireplaces are safe and ventilated."
            ),
            during = listOf(
                "Wear several thin warm layers and cover your head, hands and feet.",
                "Never use charcoal or wood fires in a closed room. They can cause deadly carbon monoxide poisoning.",
                "Drink warm fluids and eat regularly.",
                "Watch for signs of hypothermia: shivering, confusion, slurred speech. Warm the person slowly and get medical help."
            ),
            after = listOf(
                "Check on elderly people and homeless people nearby.",
                "Warm frostbitten skin slowly with lukewarm (not hot) water."
            )
        ),
        SafetyGuide(
            title = "Thunderstorm & Lightning",
            emoji = "\u26A1",
            summary = "Lightning strikes and storms",
            before = listOf(
                "Check the forecast before outdoor plans.",
                "Know the nearest strong building you can shelter in."
            ),
            during = listOf(
                "When thunder roars, go indoors. Use a building or a hard-topped vehicle.",
                "Avoid open fields, hilltops, isolated trees, water and metal poles.",
                "Stay away from windows, plumbing and wired electronics. Do not use plugged-in chargers.",
                "If you cannot reach shelter, as a last resort crouch low with feet together and head tucked in."
            ),
            after = listOf(
                "Wait 30 minutes after the last thunder before going outside.",
                "Stay away from fallen wires.",
                "A person struck by lightning does not carry electricity. Call 108 or 112 and start CPR if they are not breathing."
            )
        ),
        SafetyGuide(
            title = "Air Pollution",
            emoji = "\uD83D\uDE37",
            summary = "Smog, smoke, poor air quality",
            before = listOf(
                "Check the air quality regularly.",
                "Keep N95 masks at home. Consider an air purifier."
            ),
            during = listOf(
                "Limit outdoor activity, especially for children, older people and people with asthma or heart problems.",
                "If you must go out, wear a well-fitted N95 mask. Cloth and surgical masks do not filter fine particles well.",
                "Keep windows closed and avoid outdoor exercise.",
                "Do not burn waste or wood. Keep inhalers and medicines handy."
            ),
            after = listOf(
                "Resume outdoor activity when air quality improves.",
                "See a doctor if coughing, wheezing or chest tightness continues."
            )
        )
    )

    // Finds the guide that matches an alert (returns null if there is no match).
    fun forAlertType(type: AlertType): SafetyGuide? {
        val title = when (type) {
            AlertType.RAIN_FLOOD -> "Flood"
            AlertType.EARTHQUAKE -> "Earthquake"
            AlertType.WIND -> "Cyclone & Strong Winds"
            AlertType.HEAT -> "Heatwave"
            AlertType.COLD -> "Cold Wave"
            AlertType.THUNDERSTORM -> "Thunderstorm & Lightning"
            AlertType.AIR_QUALITY -> "Air Pollution"
            AlertType.OFFICIAL -> return null
        }
        return all.firstOrNull { it.title == title }
    }

    // Converts a guide into readable text for a dialog.
    fun toText(guide: SafetyGuide): String {
        return buildString {
            append("BEFORE\n")
            guide.before.forEach { append("\u2022 ").append(it).append("\n") }
            append("\nDURING\n")
            guide.during.forEach { append("\u2022 ").append(it).append("\n") }
            append("\nAFTER\n")
            guide.after.forEach { append("\u2022 ").append(it).append("\n") }
        }.trim()
    }
}
