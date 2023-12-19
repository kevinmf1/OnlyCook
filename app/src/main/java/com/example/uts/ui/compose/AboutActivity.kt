package com.example.uts.ui.compose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.uts.R
import com.example.uts.ui.compose.theme.UTSTheme

class AboutActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            UTSTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Pages(onBack = { onBackPressedDispatcher.onBackPressed() })
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Pages(onBack: () -> Unit) {
    Column(modifier = Modifier.padding(16.dp)) {
        // Title
        TopAppBar(
            title = { Text(text = "OnlyCook") },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = Color.Black)
                }
            }
        )

        // Image
        Image(
            painter = painterResource(id = R.drawable.cook),
            contentDescription = "Your Image",
            modifier = Modifier.size(200.dp).padding(16.dp).align(Alignment.CenterHorizontally)
        )

        // Description
        Text(
            text = "OnlyCook, sebuah inovasi revolusioner di dunia kuliner, menyajikan platform global yang mempertemukan para pecinta masak dari berbagai belahan dunia. Dengan visi membentuk komunitas yang bersemangat dalam berbagi keahlian memasak, OnlyCook menjadi rumah bagi mereka yang mencintai seni memasak dan mengeksplorasi kelezatan kuliner.\n" +
                    "\n" +
                    "Di OnlyCook, pengguna seperti Anda tidak hanya menemukan ribuan resep masakan rumahan dari berbagai budaya, tetapi juga dapat berbagi kreasi kuliner pribadi mereka. Dengan antarmuka yang ramah pengguna, platform ini memungkinkan pengguna untuk dengan mudah menjelajahi berbagai kategori, mulai dari hidangan sehari-hari hingga resep khusus untuk acara istimewa.\n" +
                    "\n" +
                    "Jadi, apakah Anda seorang koki berpengalaman atau seseorang yang baru memasuki dunia masak-memasak, OnlyCook adalah tempat di mana lidah dan keterampilan masak bersatu. Bergabunglah dengan komunitas global kami hari ini, temukan keajaiban rasa dari berbagai penjuru dunia, dan jadilah bagian dari revolusi kuliner di OnlyCook!",
            style = TextStyle(fontSize = 16.sp, color = Color.Black),
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Justify
        )
    }
}