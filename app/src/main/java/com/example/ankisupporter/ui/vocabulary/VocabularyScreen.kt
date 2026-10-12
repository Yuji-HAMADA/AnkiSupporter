package com.example.ankisupporter.ui.vocabulary

import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ankisupporter.data.db.WordEntity
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun VocabularyScreen(
    viewModel: VocabularyViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // ユーザー通知メッセージ (Toast)
    LaunchedEffect(Unit) {
        viewModel.userMessage.collectLatest { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
        }
    }

    // JSONファイルインポート用のピッカーランチャー
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            viewModel.importWordsFromJson(context, uri)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // タイトルとバックアップ（エクスポート・インポート）ボタン
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "単語帳・履歴",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                OutlinedButton(
                    onClick = {
                        importLauncher.launch(arrayOf("application/json", "text/*", "*/*"))
                    },
                    enabled = !uiState.isOperating,
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.FileDownload,
                        contentDescription = "インポート",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("インポート", fontSize = 12.sp)
                }

                OutlinedButton(
                    onClick = {
                        coroutineScope.launch {
                            val result = viewModel.exportWordsToJson(context)
                            result.onSuccess { uri ->
                                val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "application/json"
                                    putExtra(Intent.EXTRA_STREAM, uri)
                                    putExtra(Intent.EXTRA_SUBJECT, "Anki Supporter 単語帳バックアップ")
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                val chooser = Intent.createChooser(sendIntent, "単語帳バックアップの共有・保存")
                                context.startActivity(chooser)
                            }
                        }
                    },
                    enabled = !uiState.isOperating && uiState.words.isNotEmpty(),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    if (uiState.isOperating) {
                        CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp)
                    } else {
                        Icon(
                            imageVector = Icons.Default.FileUpload,
                            contentDescription = "エクスポート",
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("エクスポート", fontSize = 12.sp)
                }
            }
        }

        // タブ切り替え: 「マイ単語帳」 vs 「検索履歴（最新100件）」
        TabRow(
            selectedTabIndex = uiState.selectedTab.ordinal,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Tab(
                selected = uiState.selectedTab == VocabularyTab.SAVED,
                onClick = { viewModel.selectTab(VocabularyTab.SAVED) },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Bookmark, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("マイ単語帳")
                    }
                }
            )
            Tab(
                selected = uiState.selectedTab == VocabularyTab.HISTORY,
                onClick = { viewModel.selectTab(VocabularyTab.HISTORY) },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("検索履歴 (最新100件)")
                    }
                }
            )
        }

        // 検索ボックス
        OutlinedTextField(
            value = uiState.searchQuery,
            onValueChange = viewModel::setSearchQuery,
            placeholder = {
                Text(
                    if (uiState.selectedTab == VocabularyTab.SAVED)
                        "保存した単語や日本語訳で検索..."
                    else
                        "検索履歴から絞り込み..."
                )
            },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )

        // マイ単語帳タブ時のみ暗記フィルターを表示
        if (uiState.selectedTab == VocabularyTab.SAVED) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = uiState.filter == VocabFilter.ALL,
                    onClick = { viewModel.setFilter(VocabFilter.ALL) },
                    label = { Text("すべて") }
                )
                FilterChip(
                    selected = uiState.filter == VocabFilter.UNMEMORIZED,
                    onClick = { viewModel.setFilter(VocabFilter.UNMEMORIZED) },
                    label = { Text("未暗記") }
                )
                FilterChip(
                    selected = uiState.filter == VocabFilter.MEMORIZED,
                    onClick = { viewModel.setFilter(VocabFilter.MEMORIZED) },
                    label = { Text("暗記済み") }
                )
            }
        }

        // 単語一覧リスト表示
        if (uiState.words.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (uiState.searchQuery.isNotBlank()) {
                        "一致する単語がありません"
                    } else if (uiState.selectedTab == VocabularyTab.SAVED) {
                        "保存された単語はまだありません。\n検索履歴やメイン画面から★マークで単語帳に追加できます！"
                    } else {
                        "検索履歴はまだありません。\n単語や例文を検索・生成すると自動でここに記録されます。"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(uiState.words, key = { it.id }) { word ->
                    WordItemCard(
                        word = word,
                        isHistoryTab = uiState.selectedTab == VocabularyTab.HISTORY,
                        onToggleSaved = {
                            if (word.isSaved) {
                                viewModel.removeFromSaved(word)
                            } else {
                                viewModel.promoteToSaved(word)
                            }
                        },
                        onToggleMemorized = { viewModel.toggleMemorized(word) },
                        onDelete = { viewModel.deleteWord(word) },
                        onSpeakWord = { viewModel.speakWord(word.word) },
                        onSpeakSentence = { viewModel.speakSentence(word.exampleSentence) }
                    )
                }
            }
        }
    }
}

@Composable
fun WordItemCard(
    word: WordEntity,
    isHistoryTab: Boolean,
    onToggleSaved: () -> Unit,
    onToggleMemorized: () -> Unit,
    onDelete: () -> Unit,
    onSpeakWord: () -> Unit,
    onSpeakSentence: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    // マイ単語帳では暗記チェックボタン、履歴では単語帳登録スター/ブックマークボタン
                    if (!isHistoryTab) {
                        IconButton(onClick = onToggleMemorized) {
                            Icon(
                                imageVector = if (word.isMemorized) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                contentDescription = if (word.isMemorized) "暗記済み" else "未暗記",
                                tint = if (word.isMemorized) Color(0xFF16A34A) else MaterialTheme.colorScheme.outline
                            )
                        }
                    } else {
                        IconButton(onClick = onToggleSaved) {
                            Icon(
                                imageVector = if (word.isSaved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                contentDescription = if (word.isSaved) "保存済み" else "単語帳に追加",
                                tint = if (word.isSaved) Color(0xFF16A34A) else MaterialTheme.colorScheme.outline
                            )
                        }
                    }

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = word.word,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            if (word.phonetic.isNotBlank()) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = word.phonetic,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                        }
                        Text(
                            text = word.translation,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onSpeakWord) {
                        Icon(
                            imageVector = Icons.Default.VolumeUp,
                            contentDescription = "単語を発音",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    // 履歴タブで既に保存済みの場合はチェックマーク表示、未保存なら保存アクション
                    if (isHistoryTab) {
                        IconButton(onClick = onToggleSaved) {
                            Icon(
                                imageVector = if (word.isSaved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                contentDescription = if (word.isSaved) "マイ単語帳から解除" else "マイ単語帳に追加",
                                tint = if (word.isSaved) Color(0xFF16A34A) else MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    IconButton(onClick = onDelete) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "削除",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            if (word.exampleSentence.isNotBlank()) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = word.exampleSentence,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                            if (word.exampleTranslation.isNotBlank()) {
                                Text(
                                    text = word.exampleTranslation,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                        }
                        IconButton(onClick = onSpeakSentence, modifier = Modifier.size(32.dp)) {
                            Icon(
                                Icons.Default.VolumeUp,
                                contentDescription = "例文を発音",
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            // 履歴タブの場合は検索日時を小さく表示
            if (isHistoryTab && word.searchedAt > 0L) {
                val formattedDate = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault()).format(Date(word.searchedAt))
                Text(
                    text = "検索日時: $formattedDate",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}
