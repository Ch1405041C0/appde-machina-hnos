package com.machinahnos.vehiculos

/** Regla madre: EL STOCK DEFINE LAS PREGUNTAS. LAS RESPUESTAS DEFINEN LA RECOMENDACION. */
enum class BodyType { HATCHBACK, SEDAN, SUV, PICKUP, WAGON, COUPE, VAN, UTILITARIO, OTHER }
enum class Fuel { NAFTA, DIESEL, GNC, HIBRIDO, ELECTRICO, OTHER }
enum class Gearbox { MANUAL, AUTOMATICA, OTHER }
enum class Drive { FWD, RWD, AWD, FOUR_X_FOUR, OTHER }
enum class UseCase { CIUDAD, FAMILIA, RUTA, TRABAJO, CARGA, CAMPO, MIXTO }
enum class Priority { ECONOMIA, CONFIABILIDAD, CONFORT, SEGURIDAD, TECNOLOGIA, ESPACIO, PRESTACIONES, PRESENCIA }
enum class VehicleDNA { URBANO, RACIONAL, DEPORTIVO, AVENTURERO, ELEGANTE, FAMILIAR, TRABAJADOR, PREMIUM, JOVEN, ROBUSTO, TECNOLOGICO }
enum class Feeling { COMPRA_INTELIGENTE, ME_ENCANTA, SUBIR_NIVEL, CONFIABLE, TODO_TERRENO }

data class Vehicle(
    val id:String,val brand:String,val model:String,val year:Int,val kilometers:Int,val price:Long,
    val body:BodyType,val fuel:Fuel,val gearbox:Gearbox,val drive:Drive=Drive.OTHER,val seats:Int=5,
    val uses:Set<UseCase> = emptySet(),val strengths:Set<Priority> = emptySet(),
    val dna:Set<VehicleDNA> = emptySet()
){fun kmPerYear(currentYear:Int=2026)=kilometers/(currentYear-year).coerceAtLeast(1)}

data class BuyerProfile(
    val useCase:UseCase?=null,val minYear:Int?=null,val maxYear:Int?=null,val fuel:Fuel?=null,
    val gearbox:Gearbox?=null,val bodyPreference:BodyType?=null,val needs4x4:Boolean?=null,
    val minSeats:Int?=null,val priority:Priority?=null,val wantsLowKm:Boolean?=null,val feeling:Feeling?=null
)
data class Question(val id:String,val text:String,val options:List<String>)
data class Recommendation(val vehicle:Vehicle,val score:Int,val reasons:List<String>)

object DynamicQuestionEngine{
 fun questions(stock:List<Vehicle>):List<Question>{
  if(stock.isEmpty())return emptyList();val q=mutableListOf<Question>()
  val uses=stock.flatMap{it.uses}.distinct();if(uses.size>1)q+=Question("use","Primero lo importante: ¿para qué necesitás el auto?",uses.map{it.label()}+"Un poco de todo")
  if(stock.map{it.seats}.distinct().size>1)q+=Question("seats","En un día normal, ¿cuántas personas suelen viajar?",listOf("1 o 2","3 o 4","5 o más"))
  val years=stock.map{it.year}.distinct().sorted();if(years.size>1)q+=Question("year","Con los años del auto, ¿cómo sos?",yearOptions(years))
  val fuels=stock.map{it.fuel}.distinct();if(fuels.size>1)q+=Question("fuel","¿Tenés preferencia por algún combustible?",fuels.map{it.label()}+"Me da igual")
  val boxes=stock.map{it.gearbox}.distinct();if(boxes.size>1)q+=Question("gearbox","¿Qué preferís para manejar?",boxes.map{it.label()}+"Me da igual")
  if(stock.any{it.drive==Drive.FOUR_X_FOUR||it.drive==Drive.AWD}&&stock.any{it.drive!=Drive.FOUR_X_FOUR&&it.drive!=Drive.AWD})q+=Question("terrain","¿Tu auto tiene que bancarse campo, barro o caminos complicados?",listOf("Sí, lo necesito","Puede pasar de vez en cuando","No, casi siempre asfalto"))
  val bodies=stock.map{it.body}.distinct();if(bodies.size>1)q+=Question("body","Sin pensar demasiado: ¿qué formato te atrae más?",bodies.map{it.label()}+"No tengo preferencia")
  if(stock.map{kmBand(it)}.distinct().size>1)q+=Question("km","Cuando mirás un usado, ¿cómo pensás el kilometraje?",listOf("Quiero pocos km","Prefiero equilibrio entre año y km","Si está bien cuidado, no me preocupa"))
  val strengths=stock.flatMap{it.strengths}.distinct();if(strengths.size>1)q+=Question("priority","¿Qué valorás más en tu próximo auto?",strengths.map{it.label()}+"Quiero un buen equilibrio")
  if(stock.flatMap{it.dna}.distinct().size>1)q+=Question("feeling","Última: cuando lo veas estacionado, ¿qué querés pensar?",listOf("Hice una compra inteligente","Me encanta mi auto","Subí de nivel","Tengo algo confiable","Puedo ir a cualquier lado"))
  return q
 }
 private fun yearOptions(y:List<Int>):List<String>{val min=y.first();val max=y.last();val middle=y[y.size/2];return listOf("Prefiero algo de $middle en adelante","Puedo mirar entre $min y $max","El año no me define la compra")}
 private fun kmBand(v:Vehicle)=when{v.kilometers<=40_000->"LOW";v.kmPerYear()<=10_000->"LOW_USE";v.kmPerYear()>=25_000->"HIGH_USE";else->"MEDIUM"}
}

object RecommendationEngine{
 fun recommend(stock:List<Vehicle>,p:BuyerProfile,limit:Int=3)=stock.filter{hardFilters(it,p)}.map{score(it,p)}.sortedByDescending{it.score}.take(limit)
 private fun hardFilters(v:Vehicle,p:BuyerProfile):Boolean{if(p.minYear!=null&&v.year<p.minYear)return false;if(p.maxYear!=null&&v.year>p.maxYear)return false;if(p.minSeats!=null&&v.seats<p.minSeats)return false;if(p.needs4x4==true&&v.drive!=Drive.FOUR_X_FOUR&&v.drive!=Drive.AWD)return false;return true}
 private fun score(v:Vehicle,p:BuyerProfile):Recommendation{
  var s=35;val r=mutableListOf<String>()
  if(p.useCase!=null){if(p.useCase in v.uses){s+=22;r+="encaja con el uso que le vas a dar"}else s-=8}
  if(p.fuel!=null){if(p.fuel==v.fuel){s+=7;r+="usa el combustible que preferís"}else s-=3}
  if(p.gearbox!=null){if(p.gearbox==v.gearbox){s+=9;r+="tiene la caja que preferís"}else s-=5}
  if(p.bodyPreference!=null&&p.bodyPreference==v.body){s+=8;r+="tiene el formato que te atrae"}
  if(p.priority!=null){if(p.priority in v.strengths){s+=16;r+="se destaca en ${p.priority.label().lowercase()}"}else s-=4}
  if(p.wantsLowKm==true)when{v.kilometers<=40_000->{s+=10;r+="tiene kilometraje bajo"};v.kmPerYear()<=10_000->{s+=7;r+="tuvo poco uso anual"};v.kmPerYear()>=25_000->s-=10}
  val dnaMatch=matchingDna(p.feeling,v.dna)
  if(dnaMatch!=null){s+=18;r+="su personalidad ${dnaMatch.label().lowercase()} va con lo que buscás"}
  return Recommendation(v,s.coerceIn(0,100),r.distinct())
 }
 private fun matchingDna(feeling:Feeling?,dna:Set<VehicleDNA>):VehicleDNA?{val wanted=when(feeling){Feeling.COMPRA_INTELIGENTE->listOf(VehicleDNA.RACIONAL,VehicleDNA.URBANO);Feeling.ME_ENCANTA->listOf(VehicleDNA.DEPORTIVO,VehicleDNA.JOVEN,VehicleDNA.ELEGANTE);Feeling.SUBIR_NIVEL->listOf(VehicleDNA.PREMIUM,VehicleDNA.ELEGANTE,VehicleDNA.TECNOLOGICO);Feeling.CONFIABLE->listOf(VehicleDNA.RACIONAL,VehicleDNA.ROBUSTO,VehicleDNA.FAMILIAR);Feeling.TODO_TERRENO->listOf(VehicleDNA.AVENTURERO,VehicleDNA.ROBUSTO,VehicleDNA.TRABAJADOR);null->emptyList()};return wanted.firstOrNull{it in dna}}
}

private fun UseCase.label()=when(this){UseCase.CIUDAD->"Ciudad / todos los días";UseCase.FAMILIA->"Familia";UseCase.RUTA->"Ruta / viajes";UseCase.TRABAJO->"Trabajo";UseCase.CARGA->"Carga / reparto";UseCase.CAMPO->"Campo";UseCase.MIXTO->"Uso mixto"}
private fun Fuel.label()=when(this){Fuel.NAFTA->"Nafta";Fuel.DIESEL->"Diésel";Fuel.GNC->"GNC";Fuel.HIBRIDO->"Híbrido";Fuel.ELECTRICO->"Eléctrico";Fuel.OTHER->"Otro"}
private fun Gearbox.label()=when(this){Gearbox.MANUAL->"Manual";Gearbox.AUTOMATICA->"Automático";Gearbox.OTHER->"Otra"}
private fun BodyType.label()=when(this){BodyType.HATCHBACK->"Hatchback";BodyType.SEDAN->"Sedán";BodyType.SUV->"SUV";BodyType.PICKUP->"Pickup";BodyType.WAGON->"Rural / familiar";BodyType.COUPE->"Coupé";BodyType.VAN->"Van";BodyType.UTILITARIO->"Utilitario";BodyType.OTHER->"Otro"}
private fun Priority.label()=when(this){Priority.ECONOMIA->"Economía";Priority.CONFIABILIDAD->"Confiabilidad";Priority.CONFORT->"Comodidad";Priority.SEGURIDAD->"Seguridad";Priority.TECNOLOGIA->"Tecnología";Priority.ESPACIO->"Espacio";Priority.PRESTACIONES->"Respuesta / prestaciones";Priority.PRESENCIA->"Presencia / diseño"}
private fun VehicleDNA.label()=when(this){VehicleDNA.URBANO->"urbana";VehicleDNA.RACIONAL->"racional";VehicleDNA.DEPORTIVO->"deportiva";VehicleDNA.AVENTURERO->"aventurera";VehicleDNA.ELEGANTE->"elegante";VehicleDNA.FAMILIAR->"familiar";VehicleDNA.TRABAJADOR->"de trabajo";VehicleDNA.PREMIUM->"premium";VehicleDNA.JOVEN->"joven";VehicleDNA.ROBUSTO->"robusta";VehicleDNA.TECNOLOGICO->"tecnológica"}
