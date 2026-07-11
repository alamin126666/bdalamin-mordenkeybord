package com.modernkey.keyboard.ui.keyboard

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.modernkey.keyboard.R
import com.modernkey.keyboard.font.FontStyle

class FontBarView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : RecyclerView(context, attrs, defStyleAttr) {

    interface OnFontSelectedListener {
        fun onFontSelected(style: FontStyle)
    }

    var listener: OnFontSelectedListener? = null
    private val adapter = FontChipAdapter()

    init {
        layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        setAdapter(adapter)
        setPadding(8, 4, 8, 4)
        clipToPadding = false
    }

    fun setSelectedFont(style: FontStyle) {
        adapter.setSelected(style)
    }

    inner class FontChipAdapter : RecyclerView.Adapter<FontChipAdapter.ViewHolder>() {

        private val styles = FontStyle.entries
        private var selectedIndex = 0

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val tvFontName: TextView = view.findViewById(R.id.tv_font_name)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_font_chip, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val style = styles[position]
            holder.tvFontName.text = style.preview
            holder.tvFontName.contentDescription = style.displayName
            holder.tvFontName.isSelected = position == selectedIndex

            holder.itemView.setOnClickListener {
                val prev = selectedIndex
                selectedIndex = position
                notifyItemChanged(prev)
                notifyItemChanged(position)
                listener?.onFontSelected(style)
            }
        }

        override fun getItemCount() = styles.size

        fun setSelected(style: FontStyle) {
            val prev = selectedIndex
            selectedIndex = styles.indexOf(style).coerceAtLeast(0)
            notifyItemChanged(prev)
            notifyItemChanged(selectedIndex)
        }
    }
}
