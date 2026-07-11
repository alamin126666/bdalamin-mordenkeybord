package com.modernkey.keyboard.ui.emoji

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.modernkey.keyboard.R
import com.modernkey.keyboard.emoji.EmojiCategory
import com.modernkey.keyboard.emoji.EmojiItem
import com.modernkey.keyboard.emoji.EmojiRepository

class EmojiPanelView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    interface OnEmojiClickListener {
        fun onEmojiClicked(emoji: EmojiItem)
        fun onBackspaceClicked()
    }

    var listener: OnEmojiClickListener? = null

    private val repository = EmojiRepository.instance
    private val emojiAdapter = EmojiGridAdapter()
    private val tabAdapter   = CategoryTabAdapter()
    private var currentCategory = EmojiCategory.SMILEYS

    private lateinit var rvEmojis: RecyclerView
    private lateinit var rvTabs  : RecyclerView
    private lateinit var etSearch: EditText

    init {
        orientation = VERTICAL
        inflate(context, R.layout.view_emoji_panel, this)
        setupViews()
    }

    private fun setupViews() {
        etSearch = findViewById(R.id.et_emoji_search)
        rvTabs   = findViewById(R.id.rv_emoji_tabs)
        rvEmojis = findViewById(R.id.rv_emojis)

        rvTabs.layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
        rvTabs.adapter = tabAdapter
        tabAdapter.setOnTabClickListener { category ->
            currentCategory = category
            loadCategory(category)
        }

        rvEmojis.layoutManager = GridLayoutManager(context, 8)
        rvEmojis.adapter = emojiAdapter
        emojiAdapter.setOnEmojiClickListener { emoji ->
            repository.addToRecent(emoji)
            listener?.onEmojiClicked(emoji)
        }

        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val q = s?.toString() ?: ""
                if (q.isEmpty()) loadCategory(currentCategory)
                else emojiAdapter.submitList(repository.search(q))
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        loadCategory(currentCategory)
    }

    fun refresh() { loadCategory(currentCategory) }

    private fun loadCategory(category: EmojiCategory) {
        emojiAdapter.submitList(repository.getByCategory(category))
        rvEmojis.scrollToPosition(0)
    }
}

class EmojiGridAdapter : RecyclerView.Adapter<EmojiGridAdapter.VH>() {
    private var items: List<EmojiItem> = emptyList()
    private var onEmojiClick: ((EmojiItem) -> Unit)? = null

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        val tv: TextView = view.findViewById(R.id.tv_emoji)
    }

    fun setOnEmojiClickListener(l: (EmojiItem) -> Unit) { onEmojiClick = l }

    fun submitList(list: List<EmojiItem>) {
        items = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_emoji, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, pos: Int) {
        val item = items[pos]
        holder.tv.text = item.emoji
        holder.tv.contentDescription = item.name
        holder.itemView.setOnClickListener { onEmojiClick?.invoke(item) }
    }

    override fun getItemCount() = items.size
}

class CategoryTabAdapter : RecyclerView.Adapter<CategoryTabAdapter.VH>() {
    private val categories = EmojiCategory.entries
    private var selectedIdx = 1 // SMILEYS by default
    private var onTabClick: ((EmojiCategory) -> Unit)? = null

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        val tv: TextView = view as TextView
    }

    fun setOnTabClickListener(l: (EmojiCategory) -> Unit) { onTabClick = l }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val tv = TextView(parent.context).apply {
            textSize = 22f
            setPadding(16, 8, 16, 8)
        }
        return VH(tv)
    }

    override fun onBindViewHolder(holder: VH, pos: Int) {
        val cat = categories[pos]
        holder.tv.text = cat.icon
        holder.tv.alpha = if (pos == selectedIdx) 1f else 0.5f
        holder.itemView.setOnClickListener {
            val prev = selectedIdx
            selectedIdx = pos
            notifyItemChanged(prev)
            notifyItemChanged(pos)
            onTabClick?.invoke(cat)
        }
    }

    override fun getItemCount() = categories.size
}
