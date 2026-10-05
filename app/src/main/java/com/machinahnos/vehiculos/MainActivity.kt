package com.machinahnos.vehiculos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Bg = Color(0xFF0B0D10)
private val CardColor = Color(0xFF171B20)
private val Accent = Color(0xFFE9FF54)
private val Muted = Color(0xFF98A2AD)

enum class Screen { HOME, QUESTIONS, RESULTS, CATALOG, DETAIL }

val demoVehicles = listOf(
    Vehicle("1","Toyota","Corolla XEI 2.0",2022,45_000,29_900_000,BodyType.SEDAN,Fuel.NAFTA,Gearbox.AUTOMATICA,Drive.FWD,5,setOf(UseCase.CIUDAD,UseCase.FAMILIA,UseCase.RUTA),setOf(Priority.CONFORT,Priority.CONFIABILIDAD,Priority.SEGURIDAD)),
    Vehicle("2","Volkswagen","Amarok Highline V6",2021,72_000,48_500_000,BodyType.PICKUP,Fuel.DIESEL,Gearbox.AUTOMATICA,Drive.FOUR_X_FOUR,5,setOf(UseCase.TRABAJO,UseCase.CARGA,UseCase.CAMPO,UseCase.RUTA),setOf(Priority.PRESTACIONES,Priority.ESPACIO,Priority.PRESENCIA)),
    Vehicle("3","Ford","Territory Titanium",2023,28_000,38_700_000,BodyType.SUV,Fuel.NAFTA,Gearbox.AUTOMATICA,Drive.FWD,5,setOf(UseCase.FAMILIA,UseCase.CIUDAD,UseCase.RUTA),setOf(Priority.TECNOLOGIA,Priority.CONFORT,Priority.ESPACIO)),
    Vehicle("4","Chevrolet","Cruze Premier",2022,39_000,27_800_000,BodyType.SEDAN,Fuel.NAFTA,Gearbox.AUTOMATICA,Drive.FWD,5,setOf(UseCase.CIUDAD,UseCase.FAMILIA,UseCase.RUTA),setOf(Priority.TECNOLOGIA,Priority.CONFORT,Priority.PRESTACIONES)),
    Vehicle("5","Jeep","Renegade Longitude",2021,61_000,25_900_000,BodyType.SUV,Fuel.NAFTA,Gearbox.AUTOMATICA,Drive.FWD,5,setOf(UseCase.CIUDAD,UseCase.FAMILIA,UseCase.MIXTO),setOf(Priority.PRESENCIA,Priority.CONFORT,Priority.ESPACIO)),
    Vehicle("6","Peugeot","208 Feline",2023,19_000,24_600_000,BodyType.HATCHBACK,Fuel.NAFTA,Gearbox.AUTOMATICA,Drive.FWD,5,setOf(UseCase.CIUDAD,UseCase.MIXTO),setOf(Priority.ECONOMIA,Priority.TECNOLOGIA,Priority.PRESENCIA))
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MaterialTheme(colorScheme=darkColorScheme(primary=Accent,background=Bg,surface=CardColor)){ MachinaApp() } }
    }
}

@Composable fun MachinaApp(){
    var screen by remember { mutableStateOf(Screen.HOME) }
    var selected by remember { mutableStateOf<Vehicle?>(null) }
    var answers by remember { mutableStateOf<Map<String,String>>(emptyMap()) }
    var query by remember { mutableStateOf("") }
    var filter by remember { mutableStateOf("Todos") }

    when(screen){
        Screen.HOME -> Home(onStart={answers=emptyMap();screen=Screen.QUESTIONS},onCatalog={screen=Screen.CATALOG})
        Screen.QUESTIONS -> Questionnaire(onBack={screen=Screen.HOME},onFinish={answers=it;screen=Screen.RESULTS})
        Screen.RESULTS -> Results(answers,onBack={screen=Screen.HOME},onVehicle={selected=it;screen=Screen.DETAIL})
        Screen.CATALOG -> Catalog(query,{query=it},filter,{filter=it},{selected=it;screen=Screen.DETAIL},{screen=Screen.HOME})
        Screen.DETAIL -> selected?.let { VehicleDetail(it){screen=if(answers.isEmpty()) Screen.CATALOG else Screen.RESULTS} }
    }
}

@Composable fun Home(onStart:()->Unit,onCatalog:()->Unit){
    Scaffold(containerColor=Bg){pad->Column(Modifier.padding(pad).fillMaxSize().padding(22.dp),verticalArrangement=Arrangement.Center){
        Text("MACHINA HNOS",color=Accent,fontWeight=FontWeight.Black,fontSize=14.sp)
        Spacer(Modifier.height(28.dp))
        Text("No busques\nentre autos.",color=Color.White,fontWeight=FontWeight.Black,fontSize=44.sp,lineHeight=44.sp)
        Text("Encontrá el que va con vos.",color=Accent,fontWeight=FontWeight.Black,fontSize=27.sp,modifier=Modifier.padding(top=8.dp))
        Text("Contanos cómo lo vas a usar y qué valorás al manejar. Machina analiza los vehículos que tenemos hoy y te propone los que mejor encajan con vos.",color=Muted,fontSize=16.sp,lineHeight=23.sp,modifier=Modifier.padding(top=22.dp,bottom=28.dp))
        Button(onClick=onStart,modifier=Modifier.fillMaxWidth().height(58.dp),colors=ButtonDefaults.buttonColors(containerColor=Accent,contentColor=Color.Black)){Text("ENCONTRÁ TU AUTO",fontWeight=FontWeight.Black)}
        TextButton(onClick=onCatalog,modifier=Modifier.fillMaxWidth().padding(top=8.dp)){Text("Prefiero ver todos los autos",color=Color.White,fontWeight=FontWeight.Bold)}
    }}
}

@Composable fun Questionnaire(onBack:()->Unit,onFinish:(Map<String,String>)->Unit){
    val questions=remember{DynamicQuestionEngine.questions(demoVehicles)}
    var index by remember{mutableStateOf(0)}
    var answers by remember{mutableStateOf<Map<String,String>>(emptyMap())}
    if(questions.isEmpty()){LaunchedEffect(Unit){onFinish(emptyMap())};return}
    val q=questions[index]
    Scaffold(containerColor=Bg){pad->Column(Modifier.padding(pad).fillMaxSize().padding(20.dp)){
        Row(verticalAlignment=Alignment.CenterVertically){IconButton(onClick={if(index>0)index-- else onBack()}){Icon(Icons.Default.ArrowBack,"Volver",tint=Color.White)};Text("Pregunta ${index+1} de ${questions.size}",color=Muted,fontSize=12.sp)}
        LinearProgressIndicator(progress={(index+1).toFloat()/questions.size},modifier=Modifier.fillMaxWidth().padding(vertical=20.dp),color=Accent)
        Text(q.text,color=Color.White,fontWeight=FontWeight.Black,fontSize=30.sp,lineHeight=35.sp,modifier=Modifier.padding(bottom=24.dp))
        q.options.forEach{option->Card(colors=CardDefaults.cardColors(containerColor=CardColor),shape=RoundedCornerShape(14.dp),modifier=Modifier.fillMaxWidth().padding(vertical=5.dp).clickable{
            val next=answers+(q.id to option);answers=next
            if(index==questions.lastIndex)onFinish(next) else index++
        }){Text(option,color=Color.White,fontWeight=FontWeight.Bold,fontSize=16.sp,modifier=Modifier.padding(18.dp))}}
        Spacer(Modifier.weight(1f));Text("Las preguntas cambian según los vehículos disponibles.",color=Muted,fontSize=11.sp,modifier=Modifier.fillMaxWidth(),textAlign=TextAlign.Center)
    }}
}

private fun profileFrom(answers:Map<String,String>):BuyerProfile{
    val use=when{answers["use"]?.contains("Ciudad")==true->UseCase.CIUDAD;answers["use"]=="Familia"->UseCase.FAMILIA;answers["use"]?.contains("Ruta")==true->UseCase.RUTA;answers["use"]=="Trabajo"->UseCase.TRABAJO;answers["use"]?.contains("Carga")==true->UseCase.CARGA;answers["use"]=="Campo"->UseCase.CAMPO;else->null}
    val fuel=when(answers["fuel"]){"Nafta"->Fuel.NAFTA;"Diésel"->Fuel.DIESEL;"GNC"->Fuel.GNC;"Híbrido"->Fuel.HIBRIDO;"Eléctrico"->Fuel.ELECTRICO;else->null}
    val gearbox=when(answers["gearbox"]){"Manual"->Gearbox.MANUAL;"Automático"->Gearbox.AUTOMATICA;else->null}
    val body=when(answers["body"]){"Hatchback"->BodyType.HATCHBACK;"Sedán"->BodyType.SEDAN;"SUV"->BodyType.SUV;"Pickup"->BodyType.PICKUP;else->null}
    val priority=when(answers["priority"]){"Economía"->Priority.ECONOMIA;"Confiabilidad"->Priority.CONFIABILIDAD;"Comodidad"->Priority.CONFORT;"Seguridad"->Priority.SEGURIDAD;"Tecnología"->Priority.TECNOLOGIA;"Espacio"->Priority.ESPACIO;"Respuesta / prestaciones"->Priority.PRESTACIONES;"Presencia / diseño"->Priority.PRESENCIA;else->null}
    return BuyerProfile(useCase=use,fuel=fuel,gearbox=gearbox,bodyPreference=body,needs4x4=answers["terrain"]?.startsWith("Sí")==true,priority=priority,wantsLowKm=answers["km"]=="Quiero pocos km")
}

@Composable fun Results(answers:Map<String,String>,onBack:()->Unit,onVehicle:(Vehicle)->Unit){
    val recommendations=remember(answers){RecommendationEngine.recommend(demoVehicles,profileFrom(answers))}
    Scaffold(containerColor=Bg){pad->LazyColumn(Modifier.padding(pad).fillMaxSize(),contentPadding=PaddingValues(18.dp),verticalArrangement=Arrangement.spacedBy(14.dp)){
        item{IconButton(onClick=onBack){Icon(Icons.Default.ArrowBack,"Inicio",tint=Color.White)};Text("ESTOS SON TUS MACHINA",color=Accent,fontWeight=FontWeight.Black,fontSize=12.sp);Text("Los que más van\ncon vos.",color=Color.White,fontWeight=FontWeight.Black,fontSize=36.sp,lineHeight=38.sp);Text("No filtramos por presupuesto: buscamos compatibilidad con lo que necesitás y con el stock disponible.",color=Muted,modifier=Modifier.padding(top=10.dp,bottom=8.dp))}
        items(recommendations){r->RecommendationCard(r,onVehicle)}
        item{OutlinedButton(onClick=onBack,modifier=Modifier.fillMaxWidth()){Text("EMPEZAR DE NUEVO")}}
    }}
}

@Composable fun RecommendationCard(r:Recommendation,onVehicle:(Vehicle)->Unit){
    Card(colors=CardDefaults.cardColors(containerColor=CardColor),shape=RoundedCornerShape(17.dp),modifier=Modifier.fillMaxWidth().clickable{onVehicle(r.vehicle)}){Column(Modifier.padding(18.dp)){
        Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){Text("${r.vehicle.brand.uppercase()} · ${r.vehicle.year}",color=Accent,fontSize=11.sp,fontWeight=FontWeight.Bold);Text("${r.score}% COMPATIBLE",color=Accent,fontWeight=FontWeight.Black,fontSize=12.sp)}
        Text(r.vehicle.model,color=Color.White,fontWeight=FontWeight.Black,fontSize=23.sp)
        Text("${r.vehicle.kilometers.kmText()} km · ${r.vehicle.gearbox.uiLabel()} · ${r.vehicle.fuel.uiLabel()}",color=Muted,fontSize=13.sp,modifier=Modifier.padding(top=5.dp))
        if(r.reasons.isNotEmpty())Text(r.reasons.take(2).joinToString(" · "){it.replaceFirstChar{c->c.uppercase()}},color=Color.White,fontSize=13.sp,modifier=Modifier.padding(top=12.dp))
        Text(r.vehicle.price.priceText(),color=Color.White,fontWeight=FontWeight.Black,fontSize=20.sp,modifier=Modifier.padding(top=14.dp))
    }}
}

@Composable fun Catalog(query:String,onQuery:(String)->Unit,filter:String,onFilter:(String)->Unit,onVehicle:(Vehicle)->Unit,onBack:()->Unit){
    val visible=demoVehicles.filter{(filter=="Todos"||it.body.uiLabel()==filter)&&(query.isBlank()||("${it.brand} ${it.model} ${it.year}").contains(query,true))}
    Scaffold(containerColor=Bg){pad->LazyColumn(modifier=Modifier.padding(pad).fillMaxSize(),contentPadding=PaddingValues(18.dp),verticalArrangement=Arrangement.spacedBy(14.dp)){
        item{IconButton(onClick=onBack){Icon(Icons.Default.ArrowBack,"Volver",tint=Color.White)};Text("TODOS LOS MACHINA",color=Accent,fontWeight=FontWeight.Black,fontSize=12.sp);Text("Elegí a tu manera.",color=Color.White,fontWeight=FontWeight.Black,fontSize=34.sp)}
        item{OutlinedTextField(value=query,onValueChange=onQuery,modifier=Modifier.fillMaxWidth(),placeholder={Text("Buscar marca, modelo o año")},leadingIcon={Icon(Icons.Default.Search,null)},singleLine=true)}
        item{Row(horizontalArrangement=Arrangement.spacedBy(7.dp)){listOf("Todos","Hatchback","Sedán","SUV","Pickup").forEach{t->FilterChip(selected=filter==t,onClick={onFilter(t)},label={Text(t)})}}}
        items(visible){v->VehicleCard(v,onVehicle)}
    }}
}

@Composable fun VehicleCard(v:Vehicle,onVehicle:(Vehicle)->Unit){Card(colors=CardDefaults.cardColors(containerColor=CardColor),shape=RoundedCornerShape(16.dp),modifier=Modifier.fillMaxWidth().clickable{onVehicle(v)}){Box(Modifier.fillMaxWidth().height(130.dp).background(Color(0xFF242A30)),contentAlignment=Alignment.Center){Icon(Icons.Default.DirectionsCar,null,tint=Color(0xFF59636C),modifier=Modifier.size(70.dp))};Column(Modifier.padding(17.dp)){Text("${v.brand.uppercase()} · ${v.year}",color=Accent,fontSize=11.sp,fontWeight=FontWeight.Bold);Text(v.model,color=Color.White,fontWeight=FontWeight.Bold,fontSize=21.sp);Text("${v.kilometers.kmText()} km · ${v.gearbox.uiLabel()} · ${v.fuel.uiLabel()}",color=Muted,fontSize=13.sp,modifier=Modifier.padding(vertical=8.dp));Text(v.price.priceText(),color=Color.White,fontWeight=FontWeight.Black,fontSize=19.sp)}}}

@Composable fun VehicleDetail(v:Vehicle,onBack:()->Unit){var favorite by remember{mutableStateOf(false)};Scaffold(containerColor=Bg){pad->LazyColumn(Modifier.padding(pad).fillMaxSize(),contentPadding=PaddingValues(18.dp),verticalArrangement=Arrangement.spacedBy(16.dp)){
    item{Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){IconButton(onClick=onBack){Icon(Icons.Default.ArrowBack,"Volver",tint=Color.White)};IconButton(onClick={favorite=!favorite}){Icon(if(favorite)Icons.Default.Favorite else Icons.Default.FavoriteBorder,"Favorito",tint=if(favorite)Accent else Color.White)}}}
    item{Box(Modifier.fillMaxWidth().height(250.dp).background(Color(0xFF242A30),RoundedCornerShape(18.dp)),contentAlignment=Alignment.Center){Icon(Icons.Default.DirectionsCar,null,tint=Color(0xFF59636C),modifier=Modifier.size(105.dp))}}
    item{Text("${v.brand.uppercase()} · ${v.body.uiLabel().uppercase()}",color=Accent,fontWeight=FontWeight.Bold,fontSize=12.sp);Text(v.model,color=Color.White,fontWeight=FontWeight.Black,fontSize=32.sp);Text(v.price.priceText(),color=Color.White,fontWeight=FontWeight.Black,fontSize=25.sp,modifier=Modifier.padding(top=8.dp))}
    item{Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(8.dp)){Spec("Año",v.year.toString(),Modifier.weight(1f));Spec("Km",v.kilometers.kmText(),Modifier.weight(1f));Spec("Caja",v.gearbox.uiLabel(),Modifier.weight(1f))}}
    item{Button(onClick={},modifier=Modifier.fillMaxWidth().height(54.dp),colors=ButtonDefaults.buttonColors(containerColor=Accent,contentColor=Color.Black)){Text("QUIERO ESTE AUTO",fontWeight=FontWeight.Black)}}
}}}

@Composable fun Spec(label:String,value:String,modifier:Modifier=Modifier){Column(modifier.background(CardColor,RoundedCornerShape(10.dp)).padding(12.dp)){Text(label,color=Muted,fontSize=10.sp);Text(value,color=Color.White,fontWeight=FontWeight.Bold,fontSize=12.sp)}}
private fun Int.kmText()=String.format("%,d",this).replace(',','.')
private fun Long.priceText()="$ "+String.format("%,d",this).replace(',','.')
private fun BodyType.uiLabel()=when(this){BodyType.HATCHBACK->"Hatchback";BodyType.SEDAN->"Sedán";BodyType.SUV->"SUV";BodyType.PICKUP->"Pickup";BodyType.WAGON->"Rural";BodyType.COUPE->"Coupé";BodyType.VAN->"Van";BodyType.UTILITARIO->"Utilitario";BodyType.OTHER->"Otro"}
private fun Fuel.uiLabel()=when(this){Fuel.NAFTA->"Nafta";Fuel.DIESEL->"Diésel";Fuel.GNC->"GNC";Fuel.HIBRIDO->"Híbrido";Fuel.ELECTRICO->"Eléctrico";Fuel.OTHER->"Otro"}
private fun Gearbox.uiLabel()=when(this){Gearbox.MANUAL->"Manual";Gearbox.AUTOMATICA->"Automática";Gearbox.OTHER->"Otra"}
