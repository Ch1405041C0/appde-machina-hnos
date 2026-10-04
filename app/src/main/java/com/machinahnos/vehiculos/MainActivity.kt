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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Bg = Color(0xFF0B0D10)
private val CardColor = Color(0xFF171B20)
private val Accent = Color(0xFFE9FF54)
private val Muted = Color(0xFF98A2AD)

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
        setContent { MaterialTheme(colorScheme = darkColorScheme(primary=Accent,background=Bg,surface=CardColor)) { MachinaApp() } }
    }
}

@Composable fun MachinaApp(){
    var selected by remember { mutableStateOf<Vehicle?>(null) }
    var query by remember { mutableStateOf("") }
    var filter by remember { mutableStateOf("Todos") }
    if(selected!=null) VehicleDetail(selected!!,{selected=null}) else Catalog(query,{query=it},filter,{filter=it},{selected=it})
}

@Composable fun Catalog(query:String,onQuery:(String)->Unit,filter:String,onFilter:(String)->Unit,onVehicle:(Vehicle)->Unit){
    val visible=demoVehicles.filter{(filter=="Todos"||it.body.uiLabel()==filter)&&(query.isBlank()||("${it.brand} ${it.model} ${it.year}").contains(query,true))}
    Scaffold(containerColor=Bg,bottomBar={BottomAppBar(containerColor=CardColor){Text("MACHINA HNOS · PROTOTIPO DEMO",color=Muted,fontSize=11.sp,modifier=Modifier.fillMaxWidth(),textAlign=androidx.compose.ui.text.style.TextAlign.Center)}}){pad->
        LazyColumn(modifier=Modifier.padding(pad).fillMaxSize(),contentPadding=PaddingValues(18.dp),verticalArrangement=Arrangement.spacedBy(14.dp)){
            item{Text("MACHINA HNOS",color=Accent,fontWeight=FontWeight.Black,fontSize=15.sp);Spacer(Modifier.height(20.dp));Text("Encontrá tu\npróximo auto.",color=Color.White,fontWeight=FontWeight.Black,fontSize=40.sp,lineHeight=40.sp);Text("Vehículos seleccionados. Compra simple y segura.",color=Muted,modifier=Modifier.padding(top=10.dp,bottom=18.dp))}
            item{OutlinedTextField(value=query,onValueChange=onQuery,modifier=Modifier.fillMaxWidth(),placeholder={Text("Buscar marca, modelo o año")},leadingIcon={Icon(Icons.Default.Search,null)},singleLine=true)}
            item{Row(horizontalArrangement=Arrangement.spacedBy(7.dp)){listOf("Todos","Hatchback","Sedán","SUV","Pickup").forEach{t->FilterChip(selected=filter==t,onClick={onFilter(t)},label={Text(t)})}}}
            item{Text("${visible.size} vehículos disponibles",color=Color.White,fontWeight=FontWeight.Bold,fontSize=18.sp,modifier=Modifier.padding(top=6.dp))}
            items(visible){v->VehicleCard(v,onVehicle)}
        }
    }
}

@Composable fun VehicleCard(v:Vehicle,onVehicle:(Vehicle)->Unit){
    Card(colors=CardDefaults.cardColors(containerColor=CardColor),shape=RoundedCornerShape(16.dp),modifier=Modifier.fillMaxWidth().clickable{onVehicle(v)}){
        Box(Modifier.fillMaxWidth().height(145.dp).background(Color(0xFF242A30)),contentAlignment=Alignment.Center){Icon(Icons.Default.DirectionsCar,null,tint=Color(0xFF59636C),modifier=Modifier.size(78.dp));Text("FOTO DEL VEHÍCULO",color=Muted,fontSize=10.sp,modifier=Modifier.align(Alignment.BottomCenter).padding(12.dp))}
        Column(Modifier.padding(17.dp)){Text("${v.brand.uppercase()} · ${v.year}",color=Accent,fontSize=11.sp,fontWeight=FontWeight.Bold);Text(v.model,color=Color.White,fontWeight=FontWeight.Bold,fontSize=21.sp);Text("${v.kilometers.kmText()} km · ${v.gearbox.uiLabel()} · ${v.fuel.uiLabel()}",color=Muted,fontSize=13.sp,modifier=Modifier.padding(vertical=8.dp));Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween,verticalAlignment=Alignment.CenterVertically){Text(v.price.priceText(),color=Color.White,fontWeight=FontWeight.Black,fontSize=19.sp);Text("VER AUTO  ›",color=Accent,fontWeight=FontWeight.Bold,fontSize=12.sp)}}
    }
}

@Composable fun VehicleDetail(v:Vehicle,onBack:()->Unit){
    var favorite by remember { mutableStateOf(false) }
    Scaffold(containerColor=Bg){pad->LazyColumn(Modifier.padding(pad).fillMaxSize(),contentPadding=PaddingValues(18.dp),verticalArrangement=Arrangement.spacedBy(16.dp)){
        item{Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){IconButton(onClick=onBack){Icon(Icons.Default.ArrowBack,"Volver",tint=Color.White)};IconButton(onClick={favorite=!favorite}){Icon(if(favorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,"Favorito",tint=if(favorite) Accent else Color.White)}}}
        item{Box(Modifier.fillMaxWidth().height(260.dp).background(Color(0xFF242A30),RoundedCornerShape(18.dp)),contentAlignment=Alignment.Center){Icon(Icons.Default.DirectionsCar,null,tint=Color(0xFF59636C),modifier=Modifier.size(110.dp));Text("GALERÍA DEL VEHÍCULO",color=Muted,fontSize=11.sp,modifier=Modifier.align(Alignment.BottomCenter).padding(16.dp))}}
        item{Text("${v.brand.uppercase()} · ${v.body.uiLabel().uppercase()}",color=Accent,fontWeight=FontWeight.Bold,fontSize=12.sp);Text(v.model,color=Color.White,fontWeight=FontWeight.Black,fontSize=32.sp);Text(v.price.priceText(),color=Color.White,fontWeight=FontWeight.Black,fontSize=25.sp,modifier=Modifier.padding(top=8.dp))}
        item{Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(8.dp)){Spec("Año",v.year.toString(),Modifier.weight(1f));Spec("Km",v.kilometers.kmText(),Modifier.weight(1f));Spec("Caja",v.gearbox.uiLabel(),Modifier.weight(1f))}}
        item{Button(onClick={},modifier=Modifier.fillMaxWidth().height(54.dp),colors=ButtonDefaults.buttonColors(containerColor=Accent,contentColor=Color.Black)){Text("QUIERO ESTE AUTO",fontWeight=FontWeight.Black)}}
    }}
}

@Composable fun Spec(label:String,value:String,modifier:Modifier=Modifier){Column(modifier.background(CardColor,RoundedCornerShape(10.dp)).padding(12.dp)){Text(label,color=Muted,fontSize=10.sp);Text(value,color=Color.White,fontWeight=FontWeight.Bold,fontSize=12.sp)}}

private fun Int.kmText() = String.format("%,d",this).replace(',', '.')
private fun Long.priceText() = "$ " + String.format("%,d",this).replace(',', '.')
private fun BodyType.uiLabel() = when(this){BodyType.HATCHBACK->"Hatchback";BodyType.SEDAN->"Sedán";BodyType.SUV->"SUV";BodyType.PICKUP->"Pickup";BodyType.WAGON->"Rural";BodyType.COUPE->"Coupé";BodyType.VAN->"Van";BodyType.UTILITARIO->"Utilitario";BodyType.OTHER->"Otro"}
private fun Fuel.uiLabel() = when(this){Fuel.NAFTA->"Nafta";Fuel.DIESEL->"Diésel";Fuel.GNC->"GNC";Fuel.HIBRIDO->"Híbrido";Fuel.ELECTRICO->"Eléctrico";Fuel.OTHER->"Otro"}
private fun Gearbox.uiLabel() = when(this){Gearbox.MANUAL->"Manual";Gearbox.AUTOMATICA->"Automática";Gearbox.OTHER->"Otra"}
