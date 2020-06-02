package com.example.kotlinmessenger

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize // Parcelize sayesinde artık bu classtan oluşturulacak bir obje intent aracılığıyla başka aktivitelere aktarılabilir
class UserClass(val uid:String, val userName:String, val userImageUrl:String) : Parcelable {
    constructor() : this("","","")
}