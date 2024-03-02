package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.ZeroCornerSize
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog

import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CardElevation
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.example.myapplication.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ){
                    MainContentView()
                }
            }
        }
    }
}

class MyTask( _id: String = "", _description: String = "" )
{
    var id: String              // name of task
    var description: String     // text content
    var selected: Boolean       // trigger of opening
    init{
        id = _id
        description = _description
        selected = false
    }
}
class TaskList{
    var indexedList: List<MyTask>

    fun get(flt: String =""): List<MyTask> {
        if( flt.length ==0 ) { return  indexedList }
        return search( flt )
    }   // getter for interfacing list

    private fun search(sign: String): List<MyTask>{
        var ret = listOf<MyTask>()
        for (el in indexedList)
            if(el.description.contains(sign) || el.id.contains(sign))
                ret = ret.plus(el)
    return ret
    }   // search by id( name of task ) -> list.filter()

    fun add(newTaskName: String){
        if( newTaskName.length>0 )
        indexedList = indexedList.plus(MyTask( _id= newTaskName))
    }   // adding item to interfacing immutable list
    fun rem(id: String) {
        var ret = listOf<MyTask>()
        for (el in indexedList)
            if (el.id != id)
                ret = ret.plus(el)
        indexedList = ret
    }   // removing item from immutable list
    init{ indexedList = listOf() }
}

@Composable
fun MainContentView( modifier: Modifier = Modifier , inputList: TaskList? = null )
{
    val indexedList = inputList?: TaskList()
    val list = remember{ mutableStateOf( indexedList )}
    val bool = remember{ mutableStateOf( false )}
    val text = remember{ mutableStateOf( "" )}
    @Composable
    fun actionBtn(txt: String, action: ((String)->Unit)? = null, id:String=""){
        Spacer(modifier = Modifier.padding(5.dp))
        Text( modifier = modifier
            .padding(25.dp)
            .clickable {
                bool.value = false;
                action?.invoke(id);
                text.value = "";
            },
            style = MaterialTheme.typography.bodyMedium,
            text= txt
        )
    }   // folding buttons scoped func component

    BoxWithConstraints(
        contentAlignment = Alignment.TopCenter,
        modifier = Modifier.padding(25.dp, 15.dp)
        ){
        Column(){
            if (list.value.get().isEmpty()) {
                Text(
                    text = " try to add new task ",
                    style= MaterialTheme.typography.headlineLarge
                )
            } else {
                key( list ) {
                    SetContent(
                        modifier,
                        list.value.get( text.value )
                    )
                }
            }

            val offX = if (bool.value) Modifier else Modifier.offset(99.dp)
            val offY = if (bool.value) 0.35F else 0.9F
            Spacer(modifier = Modifier.fillMaxSize(offY))

            Button(
                onClick = { bool.value =!bool.value },
                modifier = offX
            ){
                if( !bool.value ) {     // default float button
                        Icon( Icons.Filled.Add, contentDescription = "add" )
                } else {
                    TextField(          // expand controls
                        value = text.value,
                        onValueChange = {
                            txt->text.value=txt
                        },
                        shape = MaterialTheme.shapes.large,
                        singleLine = true,
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.primary)
                            .onFocusChanged {
                                if (it.isFocused) {
                                    bool.value = true
                                }
                            }
                    ) // folding buttons isnt part of main btn
                }
            }
            if( bool.value )Row(){  // main controls expand
                actionBtn(txt = "DELETE",{ id -> list.value.rem(id) }, text.value )
                actionBtn(txt = "SAVE",  { id -> list.value.add(id) }, text.value )
                actionBtn(txt = "CLOSE") // focus interception
            }
        }
    }
}

@Composable
fun SetContent(modifier: Modifier, list: List<MyTask> = listOf()){
    LazyColumn(
        horizontalAlignment = Alignment.CenterHorizontally,
        /*modifier = Modifier.verticalScroll(), //crashes?! TODO*/
    ) {
        var inx = 0
        items(list.count()) {
            DrawListItem(list[inx++], modifier)
            Spacer(modifier = Modifier.padding(5.dp))
        }
    }
}
@Composable
fun DrawListItem(currTask: MyTask, modifier: Modifier)
{
    val clicked = remember {
        mutableStateOf(currTask.selected)
    }
    val backColor = Brush.verticalGradient(  listOf(
        Color(red = 255, green = 255, blue = 255, alpha = 99),
        Color(red = 0xAA, green = 0xAA, blue = 255)
    ))
    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
        modifier = modifier
            .background(MaterialTheme.colorScheme.background),
            shape = MaterialTheme.shapes.small
    ){
        Text(
            text  = currTask.id,
            color = Color.Black,
            style = MaterialTheme.typography.headlineLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .width(800.dp)
                .background(backColor)
                .clickable { clicked.value = true }
        )
    }
    if(clicked.value){
        Dialog(
            clicked,
            currTask
        )
    }
}

@Composable
fun Dialog( state: MutableState<Boolean>, task: MyTask ) {

    val text = remember { mutableStateOf(task.description) }
    if (state.value){
        AlertDialog(
            onDismissRequest = { state.value = false },
            title = {
                Text(
                    text = task.id,
                    modifier = Modifier.padding(5.dp)
                )
            },
            text = {
                OutlinedTextField(
                    value = text.value,
                    modifier = Modifier.padding(5.dp),
                    onValueChange = { txt -> text.value = txt },
                    shape = MaterialTheme.shapes.small
                )
            },
            confirmButton = {
                Button(onClick = {
                    task.description = text.value
                    state.value = false
                }) {
                    Text(text = "Save")
                }
            },
            dismissButton = {},
            properties = DialogProperties(
                dismissOnBackPress = true,
                dismissOnClickOutside = true
            )
        )
    }
}