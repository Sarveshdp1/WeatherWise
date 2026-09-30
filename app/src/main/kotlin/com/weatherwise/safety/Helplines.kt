package com.weatherwise.safety

// One emergency phone number.
data class Helpline(
    val number: String,
    val name: String,
    val description: String
)

/**
 * Emergency numbers for India. Change this list if you want other countries.
 */
object Helplines {
    val all: List<Helpline> = listOf(
        Helpline("112", "National Emergency Number", "Police, fire and ambulance in one number"),
        Helpline("100", "Police", "Crime, safety threats, accidents"),
        Helpline("101", "Fire Brigade", "Fire and rescue"),
        Helpline("102", "Ambulance", "Medical emergencies"),
        Helpline("108", "Emergency Ambulance", "Emergency medical and accident response"),
        Helpline("1078", "Disaster Management", "Floods, earthquakes, cyclones and other disasters"),
        Helpline("1091", "Women Helpline", "Help for women in distress"),
        Helpline("1098", "Childline", "Help for children in need")
    )
}
