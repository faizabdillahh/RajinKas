package com.rajinkas.data.local.model;

import androidx.room.Embedded;
import androidx.room.Relation;

import com.rajinkas.data.local.entity.DuesConfigEntity;
import com.rajinkas.data.local.entity.DuesPaymentEntity;

public class DuesPaymentWithConfig {
    @Embedded
    public DuesPaymentEntity payment;

    @Relation(
            parentColumn = "dues_config_id",
            entityColumn = "id"
    )
    public DuesConfigEntity config;
}
