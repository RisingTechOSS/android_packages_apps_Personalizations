/*
 * Copyright (C) 2023 the RisingOS Android Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.android.settings.preferences.ui

import android.content.Context
import android.util.AttributeSet
import android.widget.ImageView
import android.widget.TextView
import androidx.preference.Preference
import androidx.preference.PreferenceViewHolder
import com.android.settings.R

class CustomExpandDividerPreference : Preference {

    interface OnExpandListener {
        fun onExpand(expanded: Boolean)
    }

    private var mImageView: ImageView? = null
    private var mIsExpanded: Boolean = false
    private var mOnExpandListener: OnExpandListener? = null
    private var mTextView: TextView? = null
    private var mTitleContent: String? = null

    companion object {
        const val PREFERENCE_KEY = "expandable_divider"
    }

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        mIsExpanded = false
        mTitleContent = null
        layoutResource = R.layout.custom_preference_expand_divider
        key = PREFERENCE_KEY
    }

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)
        mTextView = holder.findViewById(R.id.expand_title) as TextView
        mImageView = holder.findViewById(R.id.expand_icon) as ImageView
        refreshState()
    }

    override fun onClick() {
        setExpanded(!mIsExpanded)
        mOnExpandListener?.onExpand(mIsExpanded)
    }

    fun setTitle(title: String) {
        mTitleContent = title
        refreshState()
    }

    fun setExpanded(expanded: Boolean) {
        mIsExpanded = expanded
        refreshState()
    }

    fun isExpended(): Boolean {
        return mIsExpanded
    }

    fun setOnExpandListener(listener: OnExpandListener) {
        mOnExpandListener = listener
    }

    private fun refreshState() {
        mImageView?.setImageResource(if (mIsExpanded) R.drawable.ic_expand_less else R.drawable.ic_expand_more)
        mTextView?.text = mTitleContent
    }
}
