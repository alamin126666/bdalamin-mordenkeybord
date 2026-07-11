package com.modernkey.keyboard.ui.keyboard

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.modernkey.keyboard.R
import com.modernkey.keyboard.clipboard.ClipboardItem

class ClipboardPanelView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    interface Listener {
        fun onItemClicked(item: ClipboardItem)
        fun onItemDeleted(item: ClipboardItem)
        fun onItemPinned(item: ClipboardItem)
    }

    var listener: Listener? = null
    private val adapter = ClipAdapter()

    init {
        orientation = VERTICAL
        val rv = RecyclerView(context).apply {
            layoutManager = LinearLayoutManager(context)
            this.adapter  = this@ClipboardPanelView.adapter
        }
        addView(rv, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
    }

    fun submitList(items: List<ClipboardItem>) = adapter.submitList(items)

    inner class ClipAdapter : RecyclerView.Adapter<ClipAdapter.VH>() {
        private var items: List<ClipboardItem> = emptyList()

        inner class VH(view: View) : RecyclerView.ViewHolder(view) {
            val tvContent : TextView    = view.findViewById(R.id.tv_content)
            val btnPin    : ImageButton = view.findViewById(R.id.btn_pin)
            val btnDelete : ImageButton = view.findViewById(R.id.btn_delete)
        }

        fun submitList(list: List<ClipboardItem>) { items = list; notifyDataSetChanged() }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val v = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_clipboard, parent, false)
            return VH(v)
        }

        override fun onBindViewHolder(holder: VH, pos: Int) {
            val item = items[pos]
            holder.tvContent.text = item.content
            holder.itemView.setOnClickListener { listener?.onItemClicked(item) }
            holder.btnPin.setOnClickListener   { listener?.onItemPinned(item) }
            holder.btnDelete.setOnClickListener{ listener?.onItemDeleted(item) }
        }

        override fun getItemCount() = items.size
    }
}
