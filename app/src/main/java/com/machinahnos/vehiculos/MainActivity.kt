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
private val Card = Color(0xFF171B20)
private val Accent = Color(0xFFE9FF54)
private val Muted = Color(0xFF98A2AD)

data class Vehicle(val id:Int,val brand:String,val model:String,val year:Int,val km:String,val type:String,val transmission:String,val fuel:String,val price:String)

val demoVehicles = listOf(
    Vehicle(1,"Toyota","Corolla XEI 2.0",2022,"45.000","Auto","Automática","Nafta","$ 29.900.000"),
    Vehicle(2,"Volkswagen","Amarok Highline V6",2021,"72.000","Pickup","Automática","Diésel","$ 48.500.000"),
    Vehicle(3,"Ford","Territory Titanium",2023,"28.000","SUV","Automática","Nafta","$ 38.700.000"),
    Vehicle(4,"Chevrolet","Cruze Premier",2022,"39.000","Auto","Automática","Nafta","$ 27.800.000"),
    Vehicle(5,"Jeep","Renegade Longitude",2021,"61.000","SUV","Automática","Nafta","$ 25.900.000"),
    Vehicle(6,"Peugeot","208 Feline",2023,"19.000","Auto","Automática","Nafta","$ 24.600.000")
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MaterialTheme(colorScheme = darkColorScheme(primary=Accent,background=Bg,surface=Card)) { MachinaApp() } }
    }
}

@Composable fun MachinaApp(){
    var selected by remember { mutableStateOf<Vehicle?>(null) }
    var query by remember { mutableStateOf("") }
    var filter by remember { mutableStateOf("Todos") }
    if(selected!=null) VehicleDetail(selected!!,{selected=null}) else Catalog(query,{query=it},filter,{filter=it},{selected=it})
}

@Composable fun Catalog(query:String,onQuery:(String)->Unit,filter:String,onFilter:(String)->Unit,onVehicle:(Vehicle)->Unit){
    val visible=demoVehicles.filter{(filter=="Todos"||it.type==filter)&&(query.isBlank()||("${it.brand} ${it.model} ${it.year}").contains(query,true))}
    Scaffold(containerColor=Bg,bottomBar={BottomAppBar(containerColor=Card){Text("MACHINA HNOS · PROTOTIPO DEMO",color=Muted,fontSize=11.sp,modifier=Modifier.fillMaxWidth(),textAlign=androidx.compose.ui.text.style.TextAlign.Center)}}){pad->
        LazyColumn(modifier=Modifier.padding(pad).fillMaxSize(),contentPadding=PaddingValues(18.dp),verticalArrangement=Arrangement.spacedBy(14.dp)){
            item{Text("MACHINA HNOS",color=Accent,fontWeight=FontWeight.Black,fontSize=15.sp);Spacer(Modifier.height(20.dp));Text("Encontrá tu\npróximo auto.",color=Color.White,fontWeight=FontWeight.Black,fontSize=40.sp,lineHeight=40.sp);Text("Vehículos seleccionados. Compra simple y segura.",color=Muted,modifier=Modifier.padding(top=10.dp,bottom=18.dp))}
            item{OutlinedTextField(value=query,onValueChange=onQuery,modifier=Modifier.fillMaxWidth(),placeholder={Text("Buscar marca, modelo o año")},leadingIcon={Icon(Icons.Default.Search,null)},singleLine=true)}
            item{Row(horizontalArrangement=Arrangement.spacedBy(7.dp)){listOf("Todos","Auto","SUV","Pickup").forEach{t->FilterChip(selected=filter==t,onClick={onFilter(t)},label={Text(t)})}}}
            item{Text("${visible.size} vehículos disponibles",color=Color.White,fontWeight=FontWeight.Bold,fontSize=18.sp,modifier=Modifier.padding(top=6.dp))}
            items(visible){v->VehicleCard(v,onVehicle)}
        }
    }
}

@Composable fun VehicleCard(v:Vehicle,onVehicle:(Vehicle)->Unit){
    Card(colors=CardDefaults.cardColors(containerColor=Card),shape=RoundedCornerShape(16.dp),modifier=Modifier.fillMaxWidth().clickable{onVehicle(v)}){
        Box(Modifier.fillMaxWidth().height(145.dp).background(Color(0xFF242A30)),contentAlignment=Alignment.Center){Icon(Icons.Default.DirectionsCar,null,tint=Color(0xFF59636C),modifier=Modifier.size(78.dp));Text("FOTO DEL VEHÍCULO",color=Muted,fontSize=10.sp,modifier=Modifier.align(Alignment.BottomCenter).padding(12.dp))}
        Column(Modifier.padding(17.dp)){Text("${v.brand.uppercase()} · ${v.year}",color=Accent,fontSize=11.sp,fontWeight=FontWeight.Bold);Text(v.model,color=Color.White,fontWeight=FontWeight.Bold,fontSize=21.sp);Text("${v.km} km · ${v.transmission} · ${v.fuel}",color=Muted,fontSize=13.sp,modifier=Modifier.padding(vertical=8.dp));Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween,verticalAlignment=Alignment.CenterVertically){Text(v.price,color=Color.White,fontWeight=FontWeight.Black,fontSize=19.sp);Text("VER AUTO  ›",color=Accent,fontWeight=FontWeight.Bold,fontSize=12.sp)}}
    }
}

@Composable fun VehicleDetail(v:Vehicle,onBack:()->Unit){
    var favorite by remember { mutableStateOf(false) }
    var interest by remember { mutableStateOf(false) }
    Scaffold(containerColor=Bg){pad->LazyColumn(Modifier.padding(pad).fillMaxSize(),contentPadding=PaddingValues(18.dp),verticalArrangement=Arrangement.spacedBy(16.dp)){
        item{Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){IconButton(onClick=onBack){Icon(Icons.Default.ArrowBack,"Volver",tint=Color.White)};IconButton(onClick={favorite=!favorite}){Icon(if(favorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,"Favorito",tint=if(favorite) Accent else Color.White)}}}
        item{Box(Modifier.fillMaxWidth().height(260.dp).background(Color(0xFF242A30),RoundedCornerShape(18.dp)),contentAlignment=Alignment.Center){Icon(Icons.Default.DirectionsCar,null,tint=Color(0xFF59636C),modifier=Modifier.size(110.dp));Text("GALERÍA DEL VEHÍCULO",color=Muted,fontSize=11.sp,modifier=Modifier.align(Alignment.BottomCenter).padding(16.dp))}}
        item{Text("${v.brand.uppercase()} · ${v.type.uppercase()}",color=Accent,fontWeight=FontWeight.Bold,fontSize=12.sp);Text(v.model,color=Color.White,fontWeight=FontWeight.Black,fontSize=32.sp);Text(v.price,color=Color.White,fontWeight=FontWeight.Black,fontSize=25.sp,modifier=Modifier.padding(top=8.dp))}
        item{Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(8.dp)){Spec("Año",v.year.toString(),Modifier.weight(1f));Spec("Km",v.km,Modifier.weight(1f));Spec("Caja",v.transmission,Modifier.weight(1f))}}
        item{Button(onClick={interest=true},modifier=Modifier.fillMaxWidth().height(54.dp),colors=ButtonDefaults.buttonColors(containerColor=Accent,contentColor=Color.Black)){Text("QUIERO ESTE AUTO",fontWeight=FontWeight.Black)}}
        item{OutlinedButton(onClick={interest=true},modifier=Modifier.fillMaxWidth().height(52.dp)){Text("HABLAR CON UN ASESOR",fontWeight=FontWeight.Bold)}}
        if(interest)item{Card(colors=CardDefaults.cardColors(containerColor=Card)){Column(Modifier.padding(18.dp)){Text("¡Perfecto!",color=Accent,fontWeight=FontWeight.Black,fontSize=20.sp);Text("Ya sabemos que te interesa ${v.brand} ${v.model}. El próximo paso será hacer unas preguntas breves para calificar la consulta antes de derivarla a un asesor.",color=Color.White,modifier=Modifier.padding(top=8.dp));Text("Integración con Chatbox: próxima etapa",color=Muted,fontSize=11.sp,modifier=Modifier.padding(top=12.dp))}}}
    }}
}

@Composable fun Spec(label:String,value:String,modifier:Modifier=Modifier){Column(modifier.background(Card,RoundedCornerShape(10.dp)).padding(12.dp)){Text(label,color=Muted,fontSize=10.sp);Text(value,color=Color.White,fontWeight=FontWeight.Bold,fontSize=12.sp)}}
