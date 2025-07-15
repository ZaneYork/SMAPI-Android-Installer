/*
 *  Copyright (C) 2022 github.com/REAndroid
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.reandroid.arsc.value;

/**
 *
 * Converted/copied from AOSP: frameworks/base/libs/androidfw/LocaleDataTables.cpp
 *
 * */

public class LocaleDataTables {

    public static final byte[][] SCRIPT_CODES = new byte[][]{
            /* 0  */ {(byte)'A', (byte)'g', (byte)'h', (byte)'b'},
            /* 1  */ {(byte)'A', (byte)'h', (byte)'o', (byte)'m'},
            /* 2  */ {(byte)'A', (byte)'r', (byte)'a', (byte)'b'},
            /* 3  */ {(byte)'A', (byte)'r', (byte)'m', (byte)'i'},
            /* 4  */ {(byte)'A', (byte)'r', (byte)'m', (byte)'n'},
            /* 5  */ {(byte)'A', (byte)'v', (byte)'s', (byte)'t'},
            /* 6  */ {(byte)'B', (byte)'a', (byte)'m', (byte)'u'},
            /* 7  */ {(byte)'B', (byte)'a', (byte)'s', (byte)'s'},
            /* 8  */ {(byte)'B', (byte)'e', (byte)'n', (byte)'g'},
            /* 9  */ {(byte)'B', (byte)'r', (byte)'a', (byte)'h'},
            /* 10 */ {(byte)'C', (byte)'a', (byte)'k', (byte)'m'},
            /* 11 */ {(byte)'C', (byte)'a', (byte)'n', (byte)'s'},
            /* 12 */ {(byte)'C', (byte)'a', (byte)'r', (byte)'i'},
            /* 13 */ {(byte)'C', (byte)'h', (byte)'a', (byte)'m'},
            /* 14 */ {(byte)'C', (byte)'h', (byte)'e', (byte)'r'},
            /* 15 */ {(byte)'C', (byte)'h', (byte)'r', (byte)'s'},
            /* 16 */ {(byte)'C', (byte)'o', (byte)'p', (byte)'t'},
            /* 17 */ {(byte)'C', (byte)'p', (byte)'r', (byte)'t'},
            /* 18 */ {(byte)'C', (byte)'y', (byte)'r', (byte)'l'},
            /* 19 */ {(byte)'D', (byte)'e', (byte)'v', (byte)'a'},
            /* 20 */ {(byte)'E', (byte)'g', (byte)'y', (byte)'p'},
            /* 21 */ {(byte)'E', (byte)'t', (byte)'h', (byte)'i'},
            /* 22 */ {(byte)'G', (byte)'e', (byte)'o', (byte)'r'},
            /* 23 */ {(byte)'G', (byte)'o', (byte)'n', (byte)'g'},
            /* 24 */ {(byte)'G', (byte)'o', (byte)'n', (byte)'m'},
            /* 25 */ {(byte)'G', (byte)'o', (byte)'t', (byte)'h'},
            /* 26 */ {(byte)'G', (byte)'r', (byte)'e', (byte)'k'},
            /* 27 */ {(byte)'G', (byte)'u', (byte)'j', (byte)'r'},
            /* 28 */ {(byte)'G', (byte)'u', (byte)'r', (byte)'u'},
            /* 29 */ {(byte)'H', (byte)'a', (byte)'n', (byte)'s'},
            /* 30 */ {(byte)'H', (byte)'a', (byte)'n', (byte)'t'},
            /* 31 */ {(byte)'H', (byte)'e', (byte)'b', (byte)'r'},
            /* 32 */ {(byte)'H', (byte)'l', (byte)'u', (byte)'w'},
            /* 33 */ {(byte)'H', (byte)'m', (byte)'n', (byte)'p'},
            /* 34 */ {(byte)'I', (byte)'t', (byte)'a', (byte)'l'},
            /* 35 */ {(byte)'J', (byte)'p', (byte)'a', (byte)'n'},
            /* 36 */ {(byte)'K', (byte)'a', (byte)'l', (byte)'i'},
            /* 37 */ {(byte)'K', (byte)'a', (byte)'n', (byte)'a'},
            /* 38 */ {(byte)'K', (byte)'a', (byte)'w', (byte)'i'},
            /* 39 */ {(byte)'K', (byte)'h', (byte)'a', (byte)'r'},
            /* 40 */ {(byte)'K', (byte)'h', (byte)'m', (byte)'r'},
            /* 41 */ {(byte)'K', (byte)'i', (byte)'t', (byte)'s'},
            /* 42 */ {(byte)'K', (byte)'n', (byte)'d', (byte)'a'},
            /* 43 */ {(byte)'K', (byte)'o', (byte)'r', (byte)'e'},
            /* 44 */ {(byte)'L', (byte)'a', (byte)'n', (byte)'a'},
            /* 45 */ {(byte)'L', (byte)'a', (byte)'o', (byte)'o'},
            /* 46 */ {(byte)'L', (byte)'a', (byte)'t', (byte)'n'},
            /* 47 */ {(byte)'L', (byte)'e', (byte)'p', (byte)'c'},
            /* 48 */ {(byte)'L', (byte)'i', (byte)'n', (byte)'a'},
            /* 49 */ {(byte)'L', (byte)'i', (byte)'s', (byte)'u'},
            /* 50 */ {(byte)'L', (byte)'y', (byte)'c', (byte)'i'},
            /* 51 */ {(byte)'L', (byte)'y', (byte)'d', (byte)'i'},
            /* 52 */ {(byte)'M', (byte)'a', (byte)'n', (byte)'d'},
            /* 53 */ {(byte)'M', (byte)'a', (byte)'n', (byte)'i'},
            /* 54 */ {(byte)'M', (byte)'e', (byte)'d', (byte)'f'},
            /* 55 */ {(byte)'M', (byte)'e', (byte)'r', (byte)'c'},
            /* 56 */ {(byte)'M', (byte)'l', (byte)'y', (byte)'m'},
            /* 57 */ {(byte)'M', (byte)'o', (byte)'n', (byte)'g'},
            /* 58 */ {(byte)'M', (byte)'r', (byte)'o', (byte)'o'},
            /* 59 */ {(byte)'M', (byte)'y', (byte)'m', (byte)'r'},
            /* 60 */ {(byte)'N', (byte)'a', (byte)'r', (byte)'b'},
            /* 61 */ {(byte)'N', (byte)'k', (byte)'o', (byte)'o'},
            /* 62 */ {(byte)'N', (byte)'s', (byte)'h', (byte)'u'},
            /* 63 */ {(byte)'O', (byte)'g', (byte)'a', (byte)'m'},
            /* 64 */ {(byte)'O', (byte)'l', (byte)'c', (byte)'k'},
            /* 65 */ {(byte)'O', (byte)'r', (byte)'k', (byte)'h'},
            /* 66 */ {(byte)'O', (byte)'r', (byte)'y', (byte)'a'},
            /* 67 */ {(byte)'O', (byte)'s', (byte)'g', (byte)'e'},
            /* 68 */ {(byte)'O', (byte)'u', (byte)'g', (byte)'r'},
            /* 69 */ {(byte)'P', (byte)'a', (byte)'u', (byte)'c'},
            /* 70 */ {(byte)'P', (byte)'h', (byte)'l', (byte)'i'},
            /* 71 */ {(byte)'P', (byte)'h', (byte)'n', (byte)'x'},
            /* 72 */ {(byte)'P', (byte)'l', (byte)'r', (byte)'d'},
            /* 73 */ {(byte)'P', (byte)'r', (byte)'t', (byte)'i'},
            /* 74 */ {(byte)'R', (byte)'o', (byte)'h', (byte)'g'},
            /* 75 */ {(byte)'R', (byte)'u', (byte)'n', (byte)'r'},
            /* 76 */ {(byte)'S', (byte)'a', (byte)'m', (byte)'r'},
            /* 77 */ {(byte)'S', (byte)'a', (byte)'r', (byte)'b'},
            /* 78 */ {(byte)'S', (byte)'a', (byte)'u', (byte)'r'},
            /* 79 */ {(byte)'S', (byte)'g', (byte)'n', (byte)'w'},
            /* 80 */ {(byte)'S', (byte)'i', (byte)'n', (byte)'h'},
            /* 81 */ {(byte)'S', (byte)'o', (byte)'g', (byte)'d'},
            /* 82 */ {(byte)'S', (byte)'o', (byte)'r', (byte)'a'},
            /* 83 */ {(byte)'S', (byte)'o', (byte)'y', (byte)'o'},
            /* 84 */ {(byte)'S', (byte)'y', (byte)'r', (byte)'c'},
            /* 85 */ {(byte)'T', (byte)'a', (byte)'l', (byte)'e'},
            /* 86 */ {(byte)'T', (byte)'a', (byte)'l', (byte)'u'},
            /* 87 */ {(byte)'T', (byte)'a', (byte)'m', (byte)'l'},
            /* 88 */ {(byte)'T', (byte)'a', (byte)'n', (byte)'g'},
            /* 89 */ {(byte)'T', (byte)'a', (byte)'v', (byte)'t'},
            /* 90 */ {(byte)'T', (byte)'e', (byte)'l', (byte)'u'},
            /* 91 */ {(byte)'T', (byte)'f', (byte)'n', (byte)'g'},
            /* 92 */ {(byte)'T', (byte)'h', (byte)'a', (byte)'a'},
            /* 93 */ {(byte)'T', (byte)'h', (byte)'a', (byte)'i'},
            /* 94 */ {(byte)'T', (byte)'i', (byte)'b', (byte)'t'},
            /* 95 */ {(byte)'T', (byte)'n', (byte)'s', (byte)'a'},
            /* 96 */ {(byte)'T', (byte)'o', (byte)'t', (byte)'o'},
            /* 97 */ {(byte)'U', (byte)'g', (byte)'a', (byte)'r'},
            /* 98 */ {(byte)'V', (byte)'a', (byte)'i', (byte)'i'},
            /* 99 */ {(byte)'W', (byte)'c', (byte)'h', (byte)'o'},
            /* 100 */ {(byte)'X', (byte)'p', (byte)'e', (byte)'o'},
            /* 101 */ {(byte)'X', (byte)'s', (byte)'u', (byte)'x'},
            /* 102 */ {(byte)'Y', (byte)'i', (byte)'i', (byte)'i'},
            /* 103 */ {(byte)'~', (byte)'~', (byte)'~', (byte)'A'},
            /* 104 */ {(byte)'~', (byte)'~', (byte)'~', (byte)'B'}
    };

    public static final int[][] LIKELY_SCRIPTS = new int[][]{
            {0x61610000, 46}, // aa -> Latn
            {0xA0000000, 46}, // aai -> Latn
            {0xA8000000, 46}, // aak -> Latn
            {0xD0000000, 46}, // aau -> Latn
            {0x61620000, 18}, // ab -> Cyrl
            {0xA0200000, 46}, // abi -> Latn
            {0xC0200000, 18}, // abq -> Cyrl
            {0xC4200000, 46}, // abr -> Latn
            {0xCC200000, 46}, // abt -> Latn
            {0xE0200000, 46}, // aby -> Latn
            {0x8C400000, 46}, // acd -> Latn
            {0x90400000, 46}, // ace -> Latn
            {0x9C400000, 46}, // ach -> Latn
            {0x80600000, 46}, // ada -> Latn
            {0x90600000, 46}, // ade -> Latn
            {0xA4600000, 46}, // adj -> Latn
            {0xBC600000, 94}, // adp -> Tibt
            {0xE0600000, 18}, // ady -> Cyrl
            {0xE4600000, 46}, // adz -> Latn
            {0x61650000,  5}, // ae -> Avst
            {0x84800000,  2}, // aeb -> Arab
            {0xE0800000, 46}, // aey -> Latn
            {0x61660000, 46}, // af -> Latn
            {0x88C00000, 46}, // agc -> Latn
            {0x8CC00000, 46}, // agd -> Latn
            {0x98C00000, 46}, // agg -> Latn
            {0xB0C00000, 46}, // agm -> Latn
            {0xB8C00000, 46}, // ago -> Latn
            {0xC0C00000, 46}, // agq -> Latn
            {0x80E00000, 46}, // aha -> Latn
            {0xACE00000, 46}, // ahl -> Latn
            {0xB8E00000,  1}, // aho -> Ahom
            {0x99200000, 46}, // ajg -> Latn
            {0xCD200000,  2}, // ajt -> Arab
            {0x616B0000, 46}, // ak -> Latn
            {0xA9400000, 101}, // akk -> Xsux
            {0x81600000, 46}, // ala -> Latn
            {0xA1600000, 46}, // ali -> Latn
            {0xB5600000, 46}, // aln -> Latn
            {0xCD600000, 18}, // alt -> Cyrl
            {0x616D0000, 21}, // am -> Ethi
            {0xB1800000, 46}, // amm -> Latn
            {0xB5800000, 46}, // amn -> Latn
            {0xB9800000, 46}, // amo -> Latn
            {0xBD800000, 46}, // amp -> Latn
            {0x616E0000, 46}, // an -> Latn
            {0x89A00000, 46}, // anc -> Latn
            {0xA9A00000, 46}, // ank -> Latn
            {0xB5A00000, 46}, // ann -> Latn
            {0xE1A00000, 46}, // any -> Latn
            {0xA5C00000, 46}, // aoj -> Latn
            {0xB1C00000, 46}, // aom -> Latn
            {0xE5C00000, 46}, // aoz -> Latn
            {0x89E00000,  2}, // apc -> Arab
            {0x8DE00000,  2}, // apd -> Arab
            {0x91E00000, 46}, // ape -> Latn
            {0xC5E00000, 46}, // apr -> Latn
            {0xC9E00000, 46}, // aps -> Latn
            {0xE5E00000, 46}, // apz -> Latn
            {0x61720000,  2}, // ar -> Arab
            {0x61725842, 104}, // ar-XB -> ~~~B
            {0x8A200000,  3}, // arc -> Armi
            {0x9E200000, 46}, // arh -> Latn
            {0xB6200000, 46}, // arn -> Latn
            {0xBA200000, 46}, // aro -> Latn
            {0xC2200000,  2}, // arq -> Arab
            {0xCA200000,  2}, // ars -> Arab
            {0xE2200000,  2}, // ary -> Arab
            {0xE6200000,  2}, // arz -> Arab
            {0x61730000,  8}, // as -> Beng
            {0x82400000, 46}, // asa -> Latn
            {0x92400000, 79}, // ase -> Sgnw
            {0x9A400000, 46}, // asg -> Latn
            {0xBA400000, 46}, // aso -> Latn
            {0xCE400000, 46}, // ast -> Latn
            {0x82600000, 46}, // ata -> Latn
            {0x9A600000, 46}, // atg -> Latn
            {0xA6600000, 46}, // atj -> Latn
            {0xE2800000, 46}, // auy -> Latn
            {0x61760000, 18}, // av -> Cyrl
            {0xAEA00000,  2}, // avl -> Arab
            {0xB6A00000, 46}, // avn -> Latn
            {0xCEA00000, 46}, // avt -> Latn
            {0xD2A00000, 46}, // avu -> Latn
            {0x82C00000, 19}, // awa -> Deva
            {0x86C00000, 46}, // awb -> Latn
            {0xBAC00000, 46}, // awo -> Latn
            {0xDEC00000, 46}, // awx -> Latn
            {0x61790000, 46}, // ay -> Latn
            {0x87000000, 46}, // ayb -> Latn
            {0x617A0000, 46}, // az -> Latn
            {0x617A4951,  2}, // az-IQ -> Arab
            {0x617A4952,  2}, // az-IR -> Arab
            {0x617A5255, 18}, // az-RU -> Cyrl
            {0x62610000, 18}, // ba -> Cyrl
            {0xAC010000,  2}, // bal -> Arab
            {0xB4010000, 46}, // ban -> Latn
            {0xBC010000, 19}, // bap -> Deva
            {0xC4010000, 46}, // bar -> Latn
            {0xC8010000, 46}, // bas -> Latn
            {0xD4010000, 46}, // bav -> Latn
            {0xDC010000,  6}, // bax -> Bamu
            {0x80210000, 46}, // bba -> Latn
            {0x84210000, 46}, // bbb -> Latn
            {0x88210000, 46}, // bbc -> Latn
            {0x8C210000, 46}, // bbd -> Latn
            {0xA4210000, 46}, // bbj -> Latn
            {0xBC210000, 46}, // bbp -> Latn
            {0xC4210000, 46}, // bbr -> Latn
            {0x94410000, 46}, // bcf -> Latn
            {0x9C410000, 46}, // bch -> Latn
            {0xA0410000, 46}, // bci -> Latn
            {0xB0410000, 46}, // bcm -> Latn
            {0xB4410000, 46}, // bcn -> Latn
            {0xB8410000, 46}, // bco -> Latn
            {0xC0410000, 21}, // bcq -> Ethi
            {0xD0410000, 46}, // bcu -> Latn
            {0x8C610000, 46}, // bdd -> Latn
            {0x62650000, 18}, // be -> Cyrl
            {0x94810000, 46}, // bef -> Latn
            {0x9C810000, 46}, // beh -> Latn
            {0xA4810000,  2}, // bej -> Arab
            {0xB0810000, 46}, // bem -> Latn
            {0xCC810000, 46}, // bet -> Latn
            {0xD8810000, 46}, // bew -> Latn
            {0xDC810000, 46}, // bex -> Latn
            {0xE4810000, 46}, // bez -> Latn
            {0x8CA10000, 46}, // bfd -> Latn
            {0xC0A10000, 87}, // bfq -> Taml
            {0xCCA10000,  2}, // bft -> Arab
            {0xE0A10000, 19}, // bfy -> Deva
            {0x62670000, 18}, // bg -> Cyrl
            {0x88C10000, 19}, // bgc -> Deva
            {0xB4C10000,  2}, // bgn -> Arab
            {0xDCC10000, 26}, // bgx -> Grek
            {0x84E10000, 19}, // bhb -> Deva
            {0x98E10000, 46}, // bhg -> Latn
            {0xA0E10000, 19}, // bhi -> Deva
            {0xACE10000, 46}, // bhl -> Latn
            {0xB8E10000, 19}, // bho -> Deva
            {0xE0E10000, 46}, // bhy -> Latn
            {0x62690000, 46}, // bi -> Latn
            {0x85010000, 46}, // bib -> Latn
            {0x99010000, 46}, // big -> Latn
            {0xA9010000, 46}, // bik -> Latn
            {0xB1010000, 46}, // bim -> Latn
            {0xB5010000, 46}, // bin -> Latn
            {0xB9010000, 46}, // bio -> Latn
            {0xC1010000, 46}, // biq -> Latn
            {0x9D210000, 46}, // bjh -> Latn
            {0xA1210000, 21}, // bji -> Ethi
            {0xA5210000, 19}, // bjj -> Deva
            {0xB5210000, 46}, // bjn -> Latn
            {0xB9210000, 46}, // bjo -> Latn
            {0xC5210000, 46}, // bjr -> Latn
            {0xCD210000, 46}, // bjt -> Latn
            {0xE5210000, 46}, // bjz -> Latn
            {0x89410000, 46}, // bkc -> Latn
            {0xB1410000, 46}, // bkm -> Latn
            {0xC1410000, 46}, // bkq -> Latn
            {0xD1410000, 46}, // bku -> Latn
            {0xD5410000, 46}, // bkv -> Latn
            {0x81610000, 46}, // bla -> Latn
            {0x99610000, 46}, // blg -> Latn
            {0xCD610000, 89}, // blt -> Tavt
            {0x626D0000, 46}, // bm -> Latn
            {0x9D810000, 46}, // bmh -> Latn
            {0xA9810000, 46}, // bmk -> Latn
            {0xC1810000, 46}, // bmq -> Latn
            {0xD1810000, 46}, // bmu -> Latn
            {0x626E0000,  8}, // bn -> Beng
            {0x99A10000, 46}, // bng -> Latn
            {0xB1A10000, 46}, // bnm -> Latn
            {0xBDA10000, 46}, // bnp -> Latn
            {0x626F0000, 94}, // bo -> Tibt
            {0xA5C10000, 46}, // boj -> Latn
            {0xB1C10000, 46}, // bom -> Latn
            {0xB5C10000, 46}, // bon -> Latn
            {0xE1E10000,  8}, // bpy -> Beng
            {0x8A010000, 46}, // bqc -> Latn
            {0xA2010000,  2}, // bqi -> Arab
            {0xBE010000, 46}, // bqp -> Latn
            {0xD6010000, 46}, // bqv -> Latn
            {0x62720000, 46}, // br -> Latn
            {0x82210000, 19}, // bra -> Deva
            {0x9E210000,  2}, // brh -> Arab
            {0xDE210000, 19}, // brx -> Deva
            {0xE6210000, 46}, // brz -> Latn
            {0x62730000, 46}, // bs -> Latn
            {0xA6410000, 46}, // bsj -> Latn
            {0xC2410000,  7}, // bsq -> Bass
            {0xCA410000, 46}, // bss -> Latn
            {0xCE410000, 21}, // bst -> Ethi
            {0xBA610000, 46}, // bto -> Latn
            {0xCE610000, 46}, // btt -> Latn
            {0xD6610000, 19}, // btv -> Deva
            {0x82810000, 18}, // bua -> Cyrl
            {0x8A810000, 46}, // buc -> Latn
            {0x8E810000, 46}, // bud -> Latn
            {0x9A810000, 46}, // bug -> Latn
            {0xAA810000, 46}, // buk -> Latn
            {0xB2810000, 46}, // bum -> Latn
            {0xBA810000, 46}, // buo -> Latn
            {0xCA810000, 46}, // bus -> Latn
            {0xD2810000, 46}, // buu -> Latn
            {0x86A10000, 46}, // bvb -> Latn
            {0x8EC10000, 46}, // bwd -> Latn
            {0xC6C10000, 46}, // bwr -> Latn
            {0x9EE10000, 46}, // bxh -> Latn
            {0x93010000, 46}, // bye -> Latn
            {0xB7010000, 21}, // byn -> Ethi
            {0xC7010000, 46}, // byr -> Latn
            {0xCB010000, 46}, // bys -> Latn
            {0xD7010000, 46}, // byv -> Latn
            {0xDF010000, 46}, // byx -> Latn
            {0x83210000, 46}, // bza -> Latn
            {0x93210000, 46}, // bze -> Latn
            {0x97210000, 46}, // bzf -> Latn
            {0x9F210000, 46}, // bzh -> Latn
            {0xDB210000, 46}, // bzw -> Latn
            {0x63610000, 46}, // ca -> Latn
            {0x8C020000, 46}, // cad -> Latn
            {0xB4020000, 46}, // can -> Latn
            {0xA4220000, 46}, // cbj -> Latn
            {0x9C420000, 46}, // cch -> Latn
            {0xBC420000, 10}, // ccp -> Cakm
            {0x63650000, 18}, // ce -> Cyrl
            {0x84820000, 46}, // ceb -> Latn
            {0x80A20000, 46}, // cfa -> Latn
            {0x98C20000, 46}, // cgg -> Latn
            {0x63680000, 46}, // ch -> Latn
            {0xA8E20000, 46}, // chk -> Latn
            {0xB0E20000, 18}, // chm -> Cyrl
            {0xB8E20000, 46}, // cho -> Latn
            {0xBCE20000, 46}, // chp -> Latn
            {0xC4E20000, 14}, // chr -> Cher
            {0x89020000, 46}, // cic -> Latn
            {0x81220000,  2}, // cja -> Arab
            {0xB1220000, 13}, // cjm -> Cham
            {0xD5220000, 46}, // cjv -> Latn
            {0x85420000,  2}, // ckb -> Arab
            {0xAD420000, 46}, // ckl -> Latn
            {0xB9420000, 46}, // cko -> Latn
            {0xE1420000, 46}, // cky -> Latn
            {0x81620000, 46}, // cla -> Latn
            {0x89620000, 46}, // clc -> Latn
            {0x91820000, 46}, // cme -> Latn
            {0x99820000, 83}, // cmg -> Soyo
            {0x636F0000, 46}, // co -> Latn
            {0xBDC20000, 16}, // cop -> Copt
            {0xC9E20000, 46}, // cps -> Latn
            {0x63720000, 11}, // cr -> Cans
            {0x9A220000, 46}, // crg -> Latn
            {0x9E220000, 18}, // crh -> Cyrl
            {0xAA220000, 11}, // crk -> Cans
            {0xAE220000, 11}, // crl -> Cans
            {0xCA220000, 46}, // crs -> Latn
            {0x63730000, 46}, // cs -> Latn
            {0x86420000, 46}, // csb -> Latn
            {0xDA420000, 11}, // csw -> Cans
            {0x8E620000, 69}, // ctd -> Pauc
            {0x63750000, 18}, // cu -> Cyrl
            {0x63760000, 18}, // cv -> Cyrl
            {0x63790000, 46}, // cy -> Latn
            {0x64610000, 46}, // da -> Latn
            {0x8C030000, 46}, // dad -> Latn
            {0x94030000, 46}, // daf -> Latn
            {0x98030000, 46}, // dag -> Latn
            {0x9C030000, 46}, // dah -> Latn
            {0xA8030000, 46}, // dak -> Latn
            {0xC4030000, 18}, // dar -> Cyrl
            {0xD4030000, 46}, // dav -> Latn
            {0x8C230000, 46}, // dbd -> Latn
            {0xC0230000, 46}, // dbq -> Latn
            {0x88430000,  2}, // dcc -> Arab
            {0xB4630000, 46}, // ddn -> Latn
            {0x64650000, 46}, // de -> Latn
            {0x8C830000, 46}, // ded -> Latn
            {0xB4830000, 46}, // den -> Latn
            {0x80C30000, 46}, // dga -> Latn
            {0x9CC30000, 46}, // dgh -> Latn
            {0xA0C30000, 46}, // dgi -> Latn
            {0xACC30000,  2}, // dgl -> Arab
            {0xC4C30000, 46}, // dgr -> Latn
            {0xE4C30000, 46}, // dgz -> Latn
            {0x81030000, 46}, // dia -> Latn
            {0x91230000, 46}, // dje -> Latn
            {0x95830000, 54}, // dmf -> Medf
            {0xA5A30000, 46}, // dnj -> Latn
            {0x85C30000, 46}, // dob -> Latn
            {0xA1C30000, 19}, // doi -> Deva
            {0xBDC30000, 46}, // dop -> Latn
            {0xD9C30000, 46}, // dow -> Latn
            {0x9E230000, 57}, // drh -> Mong
            {0xA2230000, 46}, // dri -> Latn
            {0xCA230000, 21}, // drs -> Ethi
            {0x86430000, 46}, // dsb -> Latn
            {0xB2630000, 46}, // dtm -> Latn
            {0xBE630000, 46}, // dtp -> Latn
            {0xCA630000, 46}, // dts -> Latn
            {0xE2630000, 19}, // dty -> Deva
            {0x82830000, 46}, // dua -> Latn
            {0x8A830000, 46}, // duc -> Latn
            {0x8E830000, 46}, // dud -> Latn
            {0x9A830000, 46}, // dug -> Latn
            {0x64760000, 92}, // dv -> Thaa
            {0x82A30000, 46}, // dva -> Latn
            {0xDAC30000, 46}, // dww -> Latn
            {0xBB030000, 46}, // dyo -> Latn
            {0xD3030000, 46}, // dyu -> Latn
            {0x647A0000, 94}, // dz -> Tibt
            {0x9B230000, 46}, // dzg -> Latn
            {0xD0240000, 46}, // ebu -> Latn
            {0x65650000, 46}, // ee -> Latn
            {0xA0A40000, 46}, // efi -> Latn
            {0xACC40000, 46}, // egl -> Latn
            {0xE0C40000, 20}, // egy -> Egyp
            {0x81440000, 46}, // eka -> Latn
            {0xE1440000, 36}, // eky -> Kali
            {0x656C0000, 26}, // el -> Grek
            {0x81840000, 46}, // ema -> Latn
            {0xA1840000, 46}, // emi -> Latn
            {0x656E0000, 46}, // en -> Latn
            {0x656E5841, 103}, // en-XA -> ~~~A
            {0xB5A40000, 46}, // enn -> Latn
            {0xC1A40000, 46}, // enq -> Latn
            {0x656F0000, 46}, // eo -> Latn
            {0xA2240000, 46}, // eri -> Latn
            {0x65730000, 46}, // es -> Latn
            {0x9A440000, 24}, // esg -> Gonm
            {0xD2440000, 46}, // esu -> Latn
            {0x65740000, 46}, // et -> Latn
            {0xC6640000, 46}, // etr -> Latn
            {0xCE640000, 34}, // ett -> Ital
            {0xD2640000, 46}, // etu -> Latn
            {0xDE640000, 46}, // etx -> Latn
            {0x65750000, 46}, // eu -> Latn
            {0xBAC40000, 46}, // ewo -> Latn
            {0xCEE40000, 46}, // ext -> Latn
            {0x83240000, 46}, // eza -> Latn
            {0x66610000,  2}, // fa -> Arab
            {0x80050000, 46}, // faa -> Latn
            {0x84050000, 46}, // fab -> Latn
            {0x98050000, 46}, // fag -> Latn
            {0xA0050000, 46}, // fai -> Latn
            {0xB4050000, 46}, // fan -> Latn
            {0x66660000, 46}, // ff -> Latn
            {0xA0A50000, 46}, // ffi -> Latn
            {0xB0A50000, 46}, // ffm -> Latn
            {0x66690000, 46}, // fi -> Latn
            {0x81050000,  2}, // fia -> Arab
            {0xAD050000, 46}, // fil -> Latn
            {0xCD050000, 46}, // fit -> Latn
            {0x666A0000, 46}, // fj -> Latn
            {0xC5650000, 46}, // flr -> Latn
            {0xBD850000, 46}, // fmp -> Latn
            {0x666F0000, 46}, // fo -> Latn
            {0x8DC50000, 46}, // fod -> Latn
            {0xB5C50000, 46}, // fon -> Latn
            {0xC5C50000, 46}, // for -> Latn
            {0x91E50000, 46}, // fpe -> Latn
            {0xCA050000, 46}, // fqs -> Latn
            {0x66720000, 46}, // fr -> Latn
            {0x8A250000, 46}, // frc -> Latn
            {0xBE250000, 46}, // frp -> Latn
            {0xC6250000, 46}, // frr -> Latn
            {0xCA250000, 46}, // frs -> Latn
            {0x86850000,  2}, // fub -> Arab
            {0x8E850000, 46}, // fud -> Latn
            {0x92850000, 46}, // fue -> Latn
            {0x96850000, 46}, // fuf -> Latn
            {0x9E850000, 46}, // fuh -> Latn
            {0xC2850000, 46}, // fuq -> Latn
            {0xC6850000, 46}, // fur -> Latn
            {0xD6850000, 46}, // fuv -> Latn
            {0xE2850000, 46}, // fuy -> Latn
            {0xC6A50000, 46}, // fvr -> Latn
            {0x66790000, 46}, // fy -> Latn
            {0x67610000, 46}, // ga -> Latn
            {0x80060000, 46}, // gaa -> Latn
            {0x94060000, 46}, // gaf -> Latn
            {0x98060000, 46}, // gag -> Latn
            {0x9C060000, 46}, // gah -> Latn
            {0xA4060000, 46}, // gaj -> Latn
            {0xB0060000, 46}, // gam -> Latn
            {0xB4060000, 29}, // gan -> Hans
            {0xD8060000, 46}, // gaw -> Latn
            {0xE0060000, 46}, // gay -> Latn
            {0x80260000, 46}, // gba -> Latn
            {0x94260000, 46}, // gbf -> Latn
            {0xB0260000, 19}, // gbm -> Deva
            {0xE0260000, 46}, // gby -> Latn
            {0xE4260000,  2}, // gbz -> Arab
            {0xC4460000, 46}, // gcr -> Latn
            {0x67640000, 46}, // gd -> Latn
            {0x90660000, 46}, // gde -> Latn
            {0xB4660000, 46}, // gdn -> Latn
            {0xC4660000, 46}, // gdr -> Latn
            {0x84860000, 46}, // geb -> Latn
            {0xA4860000, 46}, // gej -> Latn
            {0xAC860000, 46}, // gel -> Latn
            {0xE4860000, 21}, // gez -> Ethi
            {0xA8A60000, 46}, // gfk -> Latn
            {0xB4C60000, 19}, // ggn -> Deva
            {0xC8E60000, 46}, // ghs -> Latn
            {0xAD060000, 46}, // gil -> Latn
            {0xB1060000, 46}, // gim -> Latn
            {0xA9260000,  2}, // gjk -> Arab
            {0xB5260000, 46}, // gjn -> Latn
            {0xD1260000,  2}, // gju -> Arab
            {0xB5460000, 46}, // gkn -> Latn
            {0xBD460000, 46}, // gkp -> Latn
            {0x676C0000, 46}, // gl -> Latn
            {0xA9660000,  2}, // glk -> Arab
            {0xB1860000, 46}, // gmm -> Latn
            {0xD5860000, 21}, // gmv -> Ethi
            {0x676E0000, 46}, // gn -> Latn
            {0x8DA60000, 46}, // gnd -> Latn
            {0x99A60000, 46}, // gng -> Latn
            {0x8DC60000, 46}, // god -> Latn
            {0x95C60000, 21}, // gof -> Ethi
            {0xA1C60000, 46}, // goi -> Latn
            {0xB1C60000, 19}, // gom -> Deva
            {0xB5C60000, 90}, // gon -> Telu
            {0xC5C60000, 46}, // gor -> Latn
            {0xC9C60000, 46}, // gos -> Latn
            {0xCDC60000, 25}, // got -> Goth
            {0x86260000, 46}, // grb -> Latn
            {0x8A260000, 17}, // grc -> Cprt
            {0xCE260000,  8}, // grt -> Beng
            {0xDA260000, 46}, // grw -> Latn
            {0xDA460000, 46}, // gsw -> Latn
            {0x67750000, 27}, // gu -> Gujr
            {0x86860000, 46}, // gub -> Latn
            {0x8A860000, 46}, // guc -> Latn
            {0x8E860000, 46}, // gud -> Latn
            {0xC6860000, 46}, // gur -> Latn
            {0xDA860000, 46}, // guw -> Latn
            {0xDE860000, 46}, // gux -> Latn
            {0xE6860000, 46}, // guz -> Latn
            {0x67760000, 46}, // gv -> Latn
            {0x96A60000, 46}, // gvf -> Latn
            {0xC6A60000, 19}, // gvr -> Deva
            {0xCAA60000, 46}, // gvs -> Latn
            {0x8AC60000,  2}, // gwc -> Arab
            {0xA2C60000, 46}, // gwi -> Latn
            {0xCEC60000,  2}, // gwt -> Arab
            {0xA3060000, 46}, // gyi -> Latn
            {0x68610000, 46}, // ha -> Latn
            {0x6861434D,  2}, // ha-CM -> Arab
            {0x68615344,  2}, // ha-SD -> Arab
            {0x98070000, 46}, // hag -> Latn
            {0xA8070000, 29}, // hak -> Hans
            {0xB0070000, 46}, // ham -> Latn
            {0xD8070000, 46}, // haw -> Latn
            {0xE4070000,  2}, // haz -> Arab
            {0x84270000, 46}, // hbb -> Latn
            {0xE0670000, 21}, // hdy -> Ethi
            {0x68650000, 31}, // he -> Hebr
            {0xE0E70000, 46}, // hhy -> Latn
            {0x68690000, 19}, // hi -> Deva
            {0x81070000, 46}, // hia -> Latn
            {0x95070000, 46}, // hif -> Latn
            {0x99070000, 46}, // hig -> Latn
            {0x9D070000, 46}, // hih -> Latn
            {0xAD070000, 46}, // hil -> Latn
            {0x81670000, 46}, // hla -> Latn
            {0xD1670000, 32}, // hlu -> Hluw
            {0x8D870000, 72}, // hmd -> Plrd
            {0xCD870000, 46}, // hmt -> Latn
            {0x8DA70000,  2}, // hnd -> Arab
            {0x91A70000, 19}, // hne -> Deva
            {0xA5A70000, 33}, // hnj -> Hmnp
            {0xB5A70000, 46}, // hnn -> Latn
            {0xB9A70000,  2}, // hno -> Arab
            {0x686F0000, 46}, // ho -> Latn
            {0x89C70000, 19}, // hoc -> Deva
            {0xA5C70000, 19}, // hoj -> Deva
            {0xCDC70000, 46}, // hot -> Latn
            {0x68720000, 46}, // hr -> Latn
            {0x86470000, 46}, // hsb -> Latn
            {0xB6470000, 29}, // hsn -> Hans
            {0x68740000, 46}, // ht -> Latn
            {0x68750000, 46}, // hu -> Latn
            {0xA2870000, 46}, // hui -> Latn
            {0xC6870000, 46}, // hur -> Latn
            {0x68790000,  4}, // hy -> Armn
            {0x687A0000, 46}, // hz -> Latn
            {0x69610000, 46}, // ia -> Latn
            {0xB4080000, 46}, // ian -> Latn
            {0xC4080000, 46}, // iar -> Latn
            {0x80280000, 46}, // iba -> Latn
            {0x84280000, 46}, // ibb -> Latn
            {0xE0280000, 46}, // iby -> Latn
            {0x80480000, 46}, // ica -> Latn
            {0x9C480000, 46}, // ich -> Latn
            {0x69640000, 46}, // id -> Latn
            {0x8C680000, 46}, // idd -> Latn
            {0xA0680000, 46}, // idi -> Latn
            {0xD0680000, 46}, // idu -> Latn
            {0x90A80000, 46}, // ife -> Latn
            {0x69670000, 46}, // ig -> Latn
            {0x84C80000, 46}, // igb -> Latn
            {0x90C80000, 46}, // ige -> Latn
            {0x69690000, 102}, // ii -> Yiii
            {0xA5280000, 46}, // ijj -> Latn
            {0x696B0000, 46}, // ik -> Latn
            {0xA9480000, 46}, // ikk -> Latn
            {0xD9480000, 46}, // ikw -> Latn
            {0xDD480000, 46}, // ikx -> Latn
            {0xB9680000, 46}, // ilo -> Latn
            {0xB9880000, 46}, // imo -> Latn
            {0x696E0000, 46}, // in -> Latn
            {0x9DA80000, 18}, // inh -> Cyrl
            {0x696F0000, 46}, // io -> Latn
            {0xD1C80000, 46}, // iou -> Latn
            {0xA2280000, 46}, // iri -> Latn
            {0x69730000, 46}, // is -> Latn
            {0x69740000, 46}, // it -> Latn
            {0x69750000, 11}, // iu -> Cans
            {0x69770000, 31}, // iw -> Hebr
            {0xB2C80000, 46}, // iwm -> Latn
            {0xCAC80000, 46}, // iws -> Latn
            {0x9F280000, 46}, // izh -> Latn
            {0xA3280000, 46}, // izi -> Latn
            {0x6A610000, 35}, // ja -> Jpan
            {0x84090000, 46}, // jab -> Latn
            {0xB0090000, 46}, // jam -> Latn
            {0xC4090000, 46}, // jar -> Latn
            {0xB8290000, 46}, // jbo -> Latn
            {0xD0290000, 46}, // jbu -> Latn
            {0xB4890000, 46}, // jen -> Latn
            {0xA8C90000, 46}, // jgk -> Latn
            {0xB8C90000, 46}, // jgo -> Latn
            {0x6A690000, 31}, // ji -> Hebr
            {0x85090000, 46}, // jib -> Latn
            {0x89890000, 46}, // jmc -> Latn
            {0xAD890000, 19}, // jml -> Deva
            {0x82290000, 46}, // jra -> Latn
            {0xCE890000, 46}, // jut -> Latn
            {0x6A760000, 46}, // jv -> Latn
            {0x6A770000, 46}, // jw -> Latn
            {0x6B610000, 22}, // ka -> Geor
            {0x800A0000, 18}, // kaa -> Cyrl
            {0x840A0000, 46}, // kab -> Latn
            {0x880A0000, 46}, // kac -> Latn
            {0x8C0A0000, 46}, // kad -> Latn
            {0xA00A0000, 46}, // kai -> Latn
            {0xA40A0000, 46}, // kaj -> Latn
            {0xB00A0000, 46}, // kam -> Latn
            {0xB80A0000, 46}, // kao -> Latn
            {0xD80A0000, 38}, // kaw -> Kawi
            {0x8C2A0000, 18}, // kbd -> Cyrl
            {0xB02A0000, 46}, // kbm -> Latn
            {0xBC2A0000, 46}, // kbp -> Latn
            {0xC02A0000, 46}, // kbq -> Latn
            {0xDC2A0000, 46}, // kbx -> Latn
            {0xE02A0000,  2}, // kby -> Arab
            {0x984A0000, 46}, // kcg -> Latn
            {0xA84A0000, 46}, // kck -> Latn
            {0xAC4A0000, 46}, // kcl -> Latn
            {0xCC4A0000, 46}, // kct -> Latn
            {0x906A0000, 46}, // kde -> Latn
            {0x9C6A0000, 46}, // kdh -> Latn
            {0xAC6A0000, 46}, // kdl -> Latn
            {0xCC6A0000, 93}, // kdt -> Thai
            {0x808A0000, 46}, // kea -> Latn
            {0xB48A0000, 46}, // ken -> Latn
            {0xE48A0000, 46}, // kez -> Latn
            {0xB8AA0000, 46}, // kfo -> Latn
            {0xC4AA0000, 19}, // kfr -> Deva
            {0xE0AA0000, 19}, // kfy -> Deva
            {0x6B670000, 46}, // kg -> Latn
            {0x90CA0000, 46}, // kge -> Latn
            {0x94CA0000, 46}, // kgf -> Latn
            {0xBCCA0000, 46}, // kgp -> Latn
            {0x80EA0000, 46}, // kha -> Latn
            {0x84EA0000, 86}, // khb -> Talu
            {0xB4EA0000, 19}, // khn -> Deva
            {0xC0EA0000, 46}, // khq -> Latn
            {0xC8EA0000, 46}, // khs -> Latn
            {0xCCEA0000, 59}, // kht -> Mymr
            {0xD8EA0000,  2}, // khw -> Arab
            {0xE4EA0000, 46}, // khz -> Latn
            {0x6B690000, 46}, // ki -> Latn
            {0xA50A0000, 46}, // kij -> Latn
            {0xD10A0000, 46}, // kiu -> Latn
            {0xD90A0000, 46}, // kiw -> Latn
            {0x6B6A0000, 46}, // kj -> Latn
            {0x8D2A0000, 46}, // kjd -> Latn
            {0x992A0000, 45}, // kjg -> Laoo
            {0xC92A0000, 46}, // kjs -> Latn
            {0xE12A0000, 46}, // kjy -> Latn
            {0x6B6B0000, 18}, // kk -> Cyrl
            {0x6B6B4146,  2}, // kk-AF -> Arab
            {0x6B6B434E,  2}, // kk-CN -> Arab
            {0x6B6B4952,  2}, // kk-IR -> Arab
            {0x6B6B4D4E,  2}, // kk-MN -> Arab
            {0x894A0000, 46}, // kkc -> Latn
            {0xA54A0000, 46}, // kkj -> Latn
            {0x6B6C0000, 46}, // kl -> Latn
            {0xB56A0000, 46}, // kln -> Latn
            {0xC16A0000, 46}, // klq -> Latn
            {0xCD6A0000, 46}, // klt -> Latn
            {0xDD6A0000, 46}, // klx -> Latn
            {0x6B6D0000, 40}, // km -> Khmr
            {0x858A0000, 46}, // kmb -> Latn
            {0x9D8A0000, 46}, // kmh -> Latn
            {0xB98A0000, 46}, // kmo -> Latn
            {0xC98A0000, 46}, // kms -> Latn
            {0xD18A0000, 46}, // kmu -> Latn
            {0xD98A0000, 46}, // kmw -> Latn
            {0x6B6E0000, 42}, // kn -> Knda
            {0x95AA0000, 46}, // knf -> Latn
            {0xBDAA0000, 46}, // knp -> Latn
            {0x6B6F0000, 43}, // ko -> Kore
            {0xA1CA0000, 18}, // koi -> Cyrl
            {0xA9CA0000, 19}, // kok -> Deva
            {0xADCA0000, 46}, // kol -> Latn
            {0xC9CA0000, 46}, // kos -> Latn
            {0xE5CA0000, 46}, // koz -> Latn
            {0x91EA0000, 46}, // kpe -> Latn
            {0x95EA0000, 46}, // kpf -> Latn
            {0xB9EA0000, 46}, // kpo -> Latn
            {0xC5EA0000, 46}, // kpr -> Latn
            {0xDDEA0000, 46}, // kpx -> Latn
            {0x860A0000, 46}, // kqb -> Latn
            {0x960A0000, 46}, // kqf -> Latn
            {0xCA0A0000, 46}, // kqs -> Latn
            {0xE20A0000, 21}, // kqy -> Ethi
            {0x6B720000, 46}, // kr -> Latn
            {0x8A2A0000, 18}, // krc -> Cyrl
            {0xA22A0000, 46}, // kri -> Latn
            {0xA62A0000, 46}, // krj -> Latn
            {0xAE2A0000, 46}, // krl -> Latn
            {0xCA2A0000, 46}, // krs -> Latn
            {0xD22A0000, 19}, // kru -> Deva
            {0x6B730000,  2}, // ks -> Arab
            {0x864A0000, 46}, // ksb -> Latn
            {0x8E4A0000, 46}, // ksd -> Latn
            {0x964A0000, 46}, // ksf -> Latn
            {0x9E4A0000, 46}, // ksh -> Latn
            {0xA64A0000, 46}, // ksj -> Latn
            {0xC64A0000, 46}, // ksr -> Latn
            {0x866A0000, 21}, // ktb -> Ethi
            {0xB26A0000, 46}, // ktm -> Latn
            {0xBA6A0000, 46}, // kto -> Latn
            {0xC66A0000, 46}, // ktr -> Latn
            {0x6B750000, 46}, // ku -> Latn
            {0x6B754952,  2}, // ku-IR -> Arab
            {0x6B754C42,  2}, // ku-LB -> Arab
            {0x868A0000, 46}, // kub -> Latn
            {0x8E8A0000, 46}, // kud -> Latn
            {0x928A0000, 46}, // kue -> Latn
            {0xA68A0000, 46}, // kuj -> Latn
            {0xB28A0000, 18}, // kum -> Cyrl
            {0xB68A0000, 46}, // kun -> Latn
            {0xBE8A0000, 46}, // kup -> Latn
            {0xCA8A0000, 46}, // kus -> Latn
            {0x6B760000, 18}, // kv -> Cyrl
            {0x9AAA0000, 46}, // kvg -> Latn
            {0xC6AA0000, 46}, // kvr -> Latn
            {0xDEAA0000,  2}, // kvx -> Arab
            {0x6B770000, 46}, // kw -> Latn
            {0xA6CA0000, 46}, // kwj -> Latn
            {0xAACA0000, 46}, // kwk -> Latn
            {0xBACA0000, 46}, // kwo -> Latn
            {0xC2CA0000, 46}, // kwq -> Latn
            {0x82EA0000, 46}, // kxa -> Latn
            {0x8AEA0000, 21}, // kxc -> Ethi
            {0x92EA0000, 46}, // kxe -> Latn
            {0xAEEA0000, 19}, // kxl -> Deva
            {0xB2EA0000, 93}, // kxm -> Thai
            {0xBEEA0000,  2}, // kxp -> Arab
            {0xDAEA0000, 46}, // kxw -> Latn
            {0xE6EA0000, 46}, // kxz -> Latn
            {0x6B790000, 18}, // ky -> Cyrl
            {0x6B79434E,  2}, // ky-CN -> Arab
            {0x6B795452, 46}, // ky-TR -> Latn
            {0x930A0000, 46}, // kye -> Latn
            {0xDF0A0000, 46}, // kyx -> Latn
            {0x9F2A0000,  2}, // kzh -> Arab
            {0xA72A0000, 46}, // kzj -> Latn
            {0xC72A0000, 46}, // kzr -> Latn
            {0xCF2A0000, 46}, // kzt -> Latn
            {0x6C610000, 46}, // la -> Latn
            {0x840B0000, 48}, // lab -> Lina
            {0x8C0B0000, 31}, // lad -> Hebr
            {0x980B0000, 46}, // lag -> Latn
            {0x9C0B0000,  2}, // lah -> Arab
            {0xA40B0000, 46}, // laj -> Latn
            {0xC80B0000, 46}, // las -> Latn
            {0x6C620000, 46}, // lb -> Latn
            {0x902B0000, 18}, // lbe -> Cyrl
            {0xD02B0000, 46}, // lbu -> Latn
            {0xD82B0000, 46}, // lbw -> Latn
            {0xB04B0000, 46}, // lcm -> Latn
            {0xBC4B0000, 93}, // lcp -> Thai
            {0x846B0000, 46}, // ldb -> Latn
            {0x8C8B0000, 46}, // led -> Latn
            {0x908B0000, 46}, // lee -> Latn
            {0xB08B0000, 46}, // lem -> Latn
            {0xBC8B0000, 47}, // lep -> Lepc
            {0xC08B0000, 46}, // leq -> Latn
            {0xD08B0000, 46}, // leu -> Latn
            {0xE48B0000, 18}, // lez -> Cyrl
            {0x6C670000, 46}, // lg -> Latn
            {0x98CB0000, 46}, // lgg -> Latn
            {0x6C690000, 46}, // li -> Latn
            {0x810B0000, 46}, // lia -> Latn
            {0x8D0B0000, 46}, // lid -> Latn
            {0x950B0000, 19}, // lif -> Deva
            {0x990B0000, 46}, // lig -> Latn
            {0x9D0B0000, 46}, // lih -> Latn
            {0xA50B0000, 46}, // lij -> Latn
            {0xAD0B0000, 46}, // lil -> Latn
            {0xC90B0000, 49}, // lis -> Lisu
            {0xBD2B0000, 46}, // ljp -> Latn
            {0xA14B0000,  2}, // lki -> Arab
            {0xCD4B0000, 46}, // lkt -> Latn
            {0x916B0000, 46}, // lle -> Latn
            {0xB56B0000, 46}, // lln -> Latn
            {0xB58B0000, 90}, // lmn -> Telu
            {0xB98B0000, 46}, // lmo -> Latn
            {0xBD8B0000, 46}, // lmp -> Latn
            {0x6C6E0000, 46}, // ln -> Latn
            {0xC9AB0000, 46}, // lns -> Latn
            {0xD1AB0000, 46}, // lnu -> Latn
            {0x6C6F0000, 45}, // lo -> Laoo
            {0xA5CB0000, 46}, // loj -> Latn
            {0xA9CB0000, 46}, // lok -> Latn
            {0xADCB0000, 46}, // lol -> Latn
            {0xC5CB0000, 46}, // lor -> Latn
            {0xC9CB0000, 46}, // los -> Latn
            {0xE5CB0000, 46}, // loz -> Latn
            {0x8A2B0000,  2}, // lrc -> Arab
            {0x6C740000, 46}, // lt -> Latn
            {0x9A6B0000, 46}, // ltg -> Latn
            {0x6C750000, 46}, // lu -> Latn
            {0x828B0000, 46}, // lua -> Latn
            {0xBA8B0000, 46}, // luo -> Latn
            {0xE28B0000, 46}, // luy -> Latn
            {0xE68B0000,  2}, // luz -> Arab
            {0x6C760000, 46}, // lv -> Latn
            {0xAECB0000, 93}, // lwl -> Thai
            {0x9F2B0000, 29}, // lzh -> Hans
            {0xE72B0000, 46}, // lzz -> Latn
            {0x8C0C0000, 46}, // mad -> Latn
            {0x940C0000, 46}, // maf -> Latn
            {0x980C0000, 19}, // mag -> Deva
            {0xA00C0000, 19}, // mai -> Deva
            {0xA80C0000, 46}, // mak -> Latn
            {0xB40C0000, 46}, // man -> Latn
            {0xB40C474E, 61}, // man-GN -> Nkoo
            {0xC80C0000, 46}, // mas -> Latn
            {0xD80C0000, 46}, // maw -> Latn
            {0xE40C0000, 46}, // maz -> Latn
            {0x9C2C0000, 46}, // mbh -> Latn
            {0xB82C0000, 46}, // mbo -> Latn
            {0xC02C0000, 46}, // mbq -> Latn
            {0xD02C0000, 46}, // mbu -> Latn
            {0xD82C0000, 46}, // mbw -> Latn
            {0xA04C0000, 46}, // mci -> Latn
            {0xBC4C0000, 46}, // mcp -> Latn
            {0xC04C0000, 46}, // mcq -> Latn
            {0xC44C0000, 46}, // mcr -> Latn
            {0xD04C0000, 46}, // mcu -> Latn
            {0x806C0000, 46}, // mda -> Latn
            {0x906C0000,  2}, // mde -> Arab
            {0x946C0000, 18}, // mdf -> Cyrl
            {0x9C6C0000, 46}, // mdh -> Latn
            {0xA46C0000, 46}, // mdj -> Latn
            {0xC46C0000, 46}, // mdr -> Latn
            {0xDC6C0000, 21}, // mdx -> Ethi
            {0x8C8C0000, 46}, // med -> Latn
            {0x908C0000, 46}, // mee -> Latn
            {0xA88C0000, 46}, // mek -> Latn
            {0xB48C0000, 46}, // men -> Latn
            {0xC48C0000, 46}, // mer -> Latn
            {0xCC8C0000, 46}, // met -> Latn
            {0xD08C0000, 46}, // meu -> Latn
            {0x80AC0000,  2}, // mfa -> Arab
            {0x90AC0000, 46}, // mfe -> Latn
            {0xB4AC0000, 46}, // mfn -> Latn
            {0xB8AC0000, 46}, // mfo -> Latn
            {0xC0AC0000, 46}, // mfq -> Latn
            {0x6D670000, 46}, // mg -> Latn
            {0x9CCC0000, 46}, // mgh -> Latn
            {0xACCC0000, 46}, // mgl -> Latn
            {0xB8CC0000, 46}, // mgo -> Latn
            {0xBCCC0000, 19}, // mgp -> Deva
            {0xE0CC0000, 46}, // mgy -> Latn
            {0x6D680000, 46}, // mh -> Latn
            {0xA0EC0000, 46}, // mhi -> Latn
            {0xACEC0000, 46}, // mhl -> Latn
            {0x6D690000, 46}, // mi -> Latn
            {0x890C0000, 46}, // mic -> Latn
            {0x950C0000, 46}, // mif -> Latn
            {0xB50C0000, 46}, // min -> Latn
            {0xD90C0000, 46}, // miw -> Latn
            {0x6D6B0000, 18}, // mk -> Cyrl
            {0xA14C0000,  2}, // mki -> Arab
            {0xAD4C0000, 46}, // mkl -> Latn
            {0xBD4C0000, 46}, // mkp -> Latn
            {0xD94C0000, 46}, // mkw -> Latn
            {0x6D6C0000, 56}, // ml -> Mlym
            {0x916C0000, 46}, // mle -> Latn
            {0xBD6C0000, 46}, // mlp -> Latn
            {0xC96C0000, 46}, // mls -> Latn
            {0xB98C0000, 46}, // mmo -> Latn
            {0xD18C0000, 46}, // mmu -> Latn
            {0xDD8C0000, 46}, // mmx -> Latn
            {0x6D6E0000, 18}, // mn -> Cyrl
            {0x6D6E434E, 57}, // mn-CN -> Mong
            {0x81AC0000, 46}, // mna -> Latn
            {0x95AC0000, 46}, // mnf -> Latn
            {0xA1AC0000,  8}, // mni -> Beng
            {0xD9AC0000, 59}, // mnw -> Mymr
            {0x6D6F0000, 46}, // mo -> Latn
            {0x81CC0000, 46}, // moa -> Latn
            {0x91CC0000, 46}, // moe -> Latn
            {0x9DCC0000, 46}, // moh -> Latn
            {0xC9CC0000, 46}, // mos -> Latn
            {0xDDCC0000, 46}, // mox -> Latn
            {0xBDEC0000, 46}, // mpp -> Latn
            {0xC9EC0000, 46}, // mps -> Latn
            {0xCDEC0000, 46}, // mpt -> Latn
            {0xDDEC0000, 46}, // mpx -> Latn
            {0xAE0C0000, 46}, // mql -> Latn
            {0x6D720000, 19}, // mr -> Deva
            {0x8E2C0000, 19}, // mrd -> Deva
            {0xA62C0000, 18}, // mrj -> Cyrl
            {0xBA2C0000, 58}, // mro -> Mroo
            {0x6D730000, 46}, // ms -> Latn
            {0x6D734343,  2}, // ms-CC -> Arab
            {0x6D740000, 46}, // mt -> Latn
            {0x8A6C0000, 46}, // mtc -> Latn
            {0x966C0000, 46}, // mtf -> Latn
            {0xA26C0000, 46}, // mti -> Latn
            {0xC66C0000, 19}, // mtr -> Deva
            {0x828C0000, 46}, // mua -> Latn
            {0xC68C0000, 46}, // mur -> Latn
            {0xCA8C0000, 46}, // mus -> Latn
            {0x82AC0000, 46}, // mva -> Latn
            {0xB6AC0000, 46}, // mvn -> Latn
            {0xE2AC0000,  2}, // mvy -> Arab
            {0xAACC0000, 46}, // mwk -> Latn
            {0xC6CC0000, 19}, // mwr -> Deva
            {0xD6CC0000, 46}, // mwv -> Latn
            {0xDACC0000, 33}, // mww -> Hmnp
            {0x8AEC0000, 46}, // mxc -> Latn
            {0xB2EC0000, 46}, // mxm -> Latn
            {0x6D790000, 59}, // my -> Mymr
            {0xAB0C0000, 46}, // myk -> Latn
            {0xB30C0000, 21}, // mym -> Ethi
            {0xD70C0000, 18}, // myv -> Cyrl
            {0xDB0C0000, 46}, // myw -> Latn
            {0xDF0C0000, 46}, // myx -> Latn
            {0xE70C0000, 52}, // myz -> Mand
            {0xAB2C0000, 46}, // mzk -> Latn
            {0xB32C0000, 46}, // mzm -> Latn
            {0xB72C0000,  2}, // mzn -> Arab
            {0xBF2C0000, 46}, // mzp -> Latn
            {0xDB2C0000, 46}, // mzw -> Latn
            {0xE72C0000, 46}, // mzz -> Latn
            {0x6E610000, 46}, // na -> Latn
            {0x880D0000, 46}, // nac -> Latn
            {0x940D0000, 46}, // naf -> Latn
            {0xA80D0000, 46}, // nak -> Latn
            {0xB40D0000, 29}, // nan -> Hans
            {0xBC0D0000, 46}, // nap -> Latn
            {0xC00D0000, 46}, // naq -> Latn
            {0xC80D0000, 46}, // nas -> Latn
            {0x6E620000, 46}, // nb -> Latn
            {0x804D0000, 46}, // nca -> Latn
            {0x904D0000, 46}, // nce -> Latn
            {0x944D0000, 46}, // ncf -> Latn
            {0x9C4D0000, 46}, // nch -> Latn
            {0xB84D0000, 46}, // nco -> Latn
            {0xD04D0000, 46}, // ncu -> Latn
            {0x6E640000, 46}, // nd -> Latn
            {0x886D0000, 46}, // ndc -> Latn
            {0xC86D0000, 46}, // nds -> Latn
            {0x6E650000, 19}, // ne -> Deva
            {0x848D0000, 46}, // neb -> Latn
            {0xD88D0000, 19}, // new -> Deva
            {0xDC8D0000, 46}, // nex -> Latn
            {0xC4AD0000, 46}, // nfr -> Latn
            {0x6E670000, 46}, // ng -> Latn
            {0x80CD0000, 46}, // nga -> Latn
            {0x84CD0000, 46}, // ngb -> Latn
            {0xACCD0000, 46}, // ngl -> Latn
            {0x84ED0000, 46}, // nhb -> Latn
            {0x90ED0000, 46}, // nhe -> Latn
            {0xD8ED0000, 46}, // nhw -> Latn
            {0x950D0000, 46}, // nif -> Latn
            {0xA10D0000, 46}, // nii -> Latn
            {0xA50D0000, 46}, // nij -> Latn
            {0xB50D0000, 46}, // nin -> Latn
            {0xD10D0000, 46}, // niu -> Latn
            {0xE10D0000, 46}, // niy -> Latn
            {0xE50D0000, 46}, // niz -> Latn
            {0xB92D0000, 46}, // njo -> Latn
            {0x994D0000, 46}, // nkg -> Latn
            {0xB94D0000, 46}, // nko -> Latn
            {0x6E6C0000, 46}, // nl -> Latn
            {0x998D0000, 46}, // nmg -> Latn
            {0xE58D0000, 46}, // nmz -> Latn
            {0x6E6E0000, 46}, // nn -> Latn
            {0x95AD0000, 46}, // nnf -> Latn
            {0x9DAD0000, 46}, // nnh -> Latn
            {0xA9AD0000, 46}, // nnk -> Latn
            {0xB1AD0000, 46}, // nnm -> Latn
            {0xBDAD0000, 99}, // nnp -> Wcho
            {0x6E6F0000, 46}, // no -> Latn
            {0x8DCD0000, 44}, // nod -> Lana
            {0x91CD0000, 19}, // noe -> Deva
            {0xB5CD0000, 75}, // non -> Runr
            {0xBDCD0000, 46}, // nop -> Latn
            {0xD1CD0000, 46}, // nou -> Latn
            {0xBA0D0000, 61}, // nqo -> Nkoo
            {0x6E720000, 46}, // nr -> Latn
            {0x862D0000, 46}, // nrb -> Latn
            {0xAA4D0000, 11}, // nsk -> Cans
            {0xB64D0000, 46}, // nsn -> Latn
            {0xBA4D0000, 46}, // nso -> Latn
            {0xCA4D0000, 46}, // nss -> Latn
            {0xCE4D0000, 95}, // nst -> Tnsa
            {0xB26D0000, 46}, // ntm -> Latn
            {0xC66D0000, 46}, // ntr -> Latn
            {0xA28D0000, 46}, // nui -> Latn
            {0xBE8D0000, 46}, // nup -> Latn
            {0xCA8D0000, 46}, // nus -> Latn
            {0xD68D0000, 46}, // nuv -> Latn
            {0xDE8D0000, 46}, // nux -> Latn
            {0x6E760000, 46}, // nv -> Latn
            {0x86CD0000, 46}, // nwb -> Latn
            {0xC2ED0000, 46}, // nxq -> Latn
            {0xC6ED0000, 46}, // nxr -> Latn
            {0x6E790000, 46}, // ny -> Latn
            {0xB30D0000, 46}, // nym -> Latn
            {0xB70D0000, 46}, // nyn -> Latn
            {0xA32D0000, 46}, // nzi -> Latn
            {0x6F630000, 46}, // oc -> Latn
            {0x6F634553, 46}, // oc-ES -> Latn
            {0x88CE0000, 46}, // ogc -> Latn
            {0x6F6A0000, 11}, // oj -> Cans
            {0xC92E0000, 11}, // ojs -> Cans
            {0x814E0000, 46}, // oka -> Latn
            {0xC54E0000, 46}, // okr -> Latn
            {0xD54E0000, 46}, // okv -> Latn
            {0x6F6D0000, 46}, // om -> Latn
            {0x99AE0000, 46}, // ong -> Latn
            {0xB5AE0000, 46}, // onn -> Latn
            {0xC9AE0000, 46}, // ons -> Latn
            {0xB1EE0000, 46}, // opm -> Latn
            {0x6F720000, 66}, // or -> Orya
            {0xBA2E0000, 46}, // oro -> Latn
            {0xD22E0000,  2}, // oru -> Arab
            {0x6F730000, 18}, // os -> Cyrl
            {0x824E0000, 67}, // osa -> Osge
            {0x826E0000,  2}, // ota -> Arab
            {0xAA6E0000, 65}, // otk -> Orkh
            {0xA28E0000, 68}, // oui -> Ougr
            {0xB32E0000, 46}, // ozm -> Latn
            {0x70610000, 28}, // pa -> Guru
            {0x7061504B,  2}, // pa-PK -> Arab
            {0x980F0000, 46}, // pag -> Latn
            {0xAC0F0000, 70}, // pal -> Phli
            {0xB00F0000, 46}, // pam -> Latn
            {0xBC0F0000, 46}, // pap -> Latn
            {0xD00F0000, 46}, // pau -> Latn
            {0xA02F0000, 46}, // pbi -> Latn
            {0x8C4F0000, 46}, // pcd -> Latn
            {0xB04F0000, 46}, // pcm -> Latn
            {0x886F0000, 46}, // pdc -> Latn
            {0xCC6F0000, 46}, // pdt -> Latn
            {0x8C8F0000, 46}, // ped -> Latn
            {0xB88F0000, 100}, // peo -> Xpeo
            {0xDC8F0000, 46}, // pex -> Latn
            {0xACAF0000, 46}, // pfl -> Latn
            {0xACEF0000,  2}, // phl -> Arab
            {0xB4EF0000, 71}, // phn -> Phnx
            {0xAD0F0000, 46}, // pil -> Latn
            {0xBD0F0000, 46}, // pip -> Latn
            {0xC90F0000, 46}, // pis -> Latn
            {0x814F0000,  9}, // pka -> Brah
            {0xB94F0000, 46}, // pko -> Latn
            {0x706C0000, 46}, // pl -> Latn
            {0x816F0000, 46}, // pla -> Latn
            {0xC98F0000, 46}, // pms -> Latn
            {0x99AF0000, 46}, // png -> Latn
            {0xB5AF0000, 46}, // pnn -> Latn
            {0xCDAF0000, 26}, // pnt -> Grek
            {0xB5CF0000, 46}, // pon -> Latn
            {0x81EF0000, 19}, // ppa -> Deva
            {0xB9EF0000, 46}, // ppo -> Latn
            {0xB20F0000, 46}, // pqm -> Latn
            {0x822F0000, 39}, // pra -> Khar
            {0x8E2F0000,  2}, // prd -> Arab
            {0x9A2F0000, 46}, // prg -> Latn
            {0x70730000,  2}, // ps -> Arab
            {0xCA4F0000, 46}, // pss -> Latn
            {0x70740000, 46}, // pt -> Latn
            {0xBE6F0000, 46}, // ptp -> Latn
            {0xD28F0000, 46}, // puu -> Latn
            {0x82CF0000, 46}, // pwa -> Latn
            {0x71750000, 46}, // qu -> Latn
            {0x8A900000, 46}, // quc -> Latn
            {0x9A900000, 46}, // qug -> Latn
            {0xA0110000, 46}, // rai -> Latn
            {0xA4110000, 19}, // raj -> Deva
            {0xB8110000, 46}, // rao -> Latn
            {0x94510000, 46}, // rcf -> Latn
            {0xA4910000, 46}, // rej -> Latn
            {0xAC910000, 46}, // rel -> Latn
            {0xC8910000, 46}, // res -> Latn
            {0xB4D10000, 46}, // rgn -> Latn
            {0x98F10000, 74}, // rhg -> Rohg
            {0x81110000, 46}, // ria -> Latn
            {0x95110000, 91}, // rif -> Tfng
            {0x95114E4C, 46}, // rif-NL -> Latn
            {0xC9310000, 19}, // rjs -> Deva
            {0xCD510000,  8}, // rkt -> Beng
            {0x726D0000, 46}, // rm -> Latn
            {0x95910000, 46}, // rmf -> Latn
            {0xB9910000, 46}, // rmo -> Latn
            {0xCD910000,  2}, // rmt -> Arab
            {0xD1910000, 46}, // rmu -> Latn
            {0x726E0000, 46}, // rn -> Latn
            {0x81B10000, 46}, // rna -> Latn
            {0x99B10000, 46}, // rng -> Latn
            {0x726F0000, 46}, // ro -> Latn
            {0x85D10000, 46}, // rob -> Latn
            {0x95D10000, 46}, // rof -> Latn
            {0xB9D10000, 46}, // roo -> Latn
            {0xBA310000, 46}, // rro -> Latn
            {0xB2710000, 46}, // rtm -> Latn
            {0x72750000, 18}, // ru -> Cyrl
            {0x92910000, 18}, // rue -> Cyrl
            {0x9A910000, 46}, // rug -> Latn
            {0x72770000, 46}, // rw -> Latn
            {0xAAD10000, 46}, // rwk -> Latn
            {0xBAD10000, 46}, // rwo -> Latn
            {0xD3110000, 37}, // ryu -> Kana
            {0x73610000, 19}, // sa -> Deva
            {0x94120000, 46}, // saf -> Latn
            {0x9C120000, 18}, // sah -> Cyrl
            {0xC0120000, 46}, // saq -> Latn
            {0xC8120000, 46}, // sas -> Latn
            {0xCC120000, 64}, // sat -> Olck
            {0xD4120000, 46}, // sav -> Latn
            {0xE4120000, 78}, // saz -> Saur
            {0x80320000, 46}, // sba -> Latn
            {0x90320000, 46}, // sbe -> Latn
            {0xBC320000, 46}, // sbp -> Latn
            {0x73630000, 46}, // sc -> Latn
            {0xA8520000, 19}, // sck -> Deva
            {0xAC520000,  2}, // scl -> Arab
            {0xB4520000, 46}, // scn -> Latn
            {0xB8520000, 46}, // sco -> Latn
            {0x73640000,  2}, // sd -> Arab
            {0x7364494E, 19}, // sd-IN -> Deva
            {0x88720000, 46}, // sdc -> Latn
            {0x9C720000,  2}, // sdh -> Arab
            {0x73650000, 46}, // se -> Latn
            {0x94920000, 46}, // sef -> Latn
            {0x9C920000, 46}, // seh -> Latn
            {0xA0920000, 46}, // sei -> Latn
            {0xC8920000, 46}, // ses -> Latn
            {0x73670000, 46}, // sg -> Latn
            {0x80D20000, 63}, // sga -> Ogam
            {0xC8D20000, 46}, // sgs -> Latn
            {0xD8D20000, 21}, // sgw -> Ethi
            {0xE4D20000, 46}, // sgz -> Latn
            {0x73680000, 46}, // sh -> Latn
            {0xA0F20000, 91}, // shi -> Tfng
            {0xA8F20000, 46}, // shk -> Latn
            {0xB4F20000, 59}, // shn -> Mymr
            {0xD0F20000,  2}, // shu -> Arab
            {0x73690000, 80}, // si -> Sinh
            {0x8D120000, 46}, // sid -> Latn
            {0x99120000, 46}, // sig -> Latn
            {0xAD120000, 46}, // sil -> Latn
            {0xB1120000, 46}, // sim -> Latn
            {0xC5320000, 46}, // sjr -> Latn
            {0x736B0000, 46}, // sk -> Latn
            {0x89520000, 46}, // skc -> Latn
            {0xC5520000,  2}, // skr -> Arab
            {0xC9520000, 46}, // sks -> Latn
            {0x736C0000, 46}, // sl -> Latn
            {0x8D720000, 46}, // sld -> Latn
            {0xA1720000, 46}, // sli -> Latn
            {0xAD720000, 46}, // sll -> Latn
            {0xE1720000, 46}, // sly -> Latn
            {0x736D0000, 46}, // sm -> Latn
            {0x81920000, 46}, // sma -> Latn
            {0x8D920000, 46}, // smd -> Latn
            {0xA5920000, 46}, // smj -> Latn
            {0xB5920000, 46}, // smn -> Latn
            {0xBD920000, 76}, // smp -> Samr
            {0xC1920000, 46}, // smq -> Latn
            {0xC9920000, 46}, // sms -> Latn
            {0x736E0000, 46}, // sn -> Latn
            {0x85B20000, 46}, // snb -> Latn
            {0x89B20000, 46}, // snc -> Latn
            {0xA9B20000, 46}, // snk -> Latn
            {0xBDB20000, 46}, // snp -> Latn
            {0xDDB20000, 46}, // snx -> Latn
            {0xE1B20000, 46}, // sny -> Latn
            {0x736F0000, 46}, // so -> Latn
            {0x99D20000, 81}, // sog -> Sogd
            {0xA9D20000, 46}, // sok -> Latn
            {0xC1D20000, 46}, // soq -> Latn
            {0xD1D20000, 93}, // sou -> Thai
            {0xE1D20000, 46}, // soy -> Latn
            {0x8DF20000, 46}, // spd -> Latn
            {0xADF20000, 46}, // spl -> Latn
            {0xC9F20000, 46}, // sps -> Latn
            {0x73710000, 46}, // sq -> Latn
            {0x73720000, 18}, // sr -> Cyrl
            {0x73724D45, 46}, // sr-ME -> Latn
            {0x7372524F, 46}, // sr-RO -> Latn
            {0x73725255, 46}, // sr-RU -> Latn
            {0x73725452, 46}, // sr-TR -> Latn
            {0x86320000, 82}, // srb -> Sora
            {0xB6320000, 46}, // srn -> Latn
            {0xC6320000, 46}, // srr -> Latn
            {0xDE320000, 19}, // srx -> Deva
            {0x73730000, 46}, // ss -> Latn
            {0x8E520000, 46}, // ssd -> Latn
            {0x9A520000, 46}, // ssg -> Latn
            {0xE2520000, 46}, // ssy -> Latn
            {0x73740000, 46}, // st -> Latn
            {0xAA720000, 46}, // stk -> Latn
            {0xC2720000, 46}, // stq -> Latn
            {0x73750000, 46}, // su -> Latn
            {0x82920000, 46}, // sua -> Latn
            {0x92920000, 46}, // sue -> Latn
            {0xAA920000, 46}, // suk -> Latn
            {0xC6920000, 46}, // sur -> Latn
            {0xCA920000, 46}, // sus -> Latn
            {0x73760000, 46}, // sv -> Latn
            {0x73770000, 46}, // sw -> Latn
            {0x86D20000,  2}, // swb -> Arab
            {0x8AD20000, 46}, // swc -> Latn
            {0x9AD20000, 46}, // swg -> Latn
            {0xBED20000, 46}, // swp -> Latn
            {0xD6D20000, 19}, // swv -> Deva
            {0xB6F20000, 46}, // sxn -> Latn
            {0xDAF20000, 46}, // sxw -> Latn
            {0xAF120000,  8}, // syl -> Beng
            {0xC7120000, 84}, // syr -> Syrc
            {0xAF320000, 46}, // szl -> Latn
            {0x74610000, 87}, // ta -> Taml
            {0xA4130000, 19}, // taj -> Deva
            {0xAC130000, 46}, // tal -> Latn
            {0xB4130000, 46}, // tan -> Latn
            {0xC0130000, 46}, // taq -> Latn
            {0x88330000, 46}, // tbc -> Latn
            {0x8C330000, 46}, // tbd -> Latn
            {0x94330000, 46}, // tbf -> Latn
            {0x98330000, 46}, // tbg -> Latn
            {0xB8330000, 46}, // tbo -> Latn
            {0xD8330000, 46}, // tbw -> Latn
            {0xE4330000, 46}, // tbz -> Latn
            {0xA0530000, 46}, // tci -> Latn
            {0xE0530000, 42}, // tcy -> Knda
            {0x8C730000, 85}, // tdd -> Tale
            {0x98730000, 19}, // tdg -> Deva
            {0x9C730000, 19}, // tdh -> Deva
            {0xD0730000, 46}, // tdu -> Latn
            {0x74650000, 90}, // te -> Telu
            {0x8C930000, 46}, // ted -> Latn
            {0xB0930000, 46}, // tem -> Latn
            {0xB8930000, 46}, // teo -> Latn
            {0xCC930000, 46}, // tet -> Latn
            {0xA0B30000, 46}, // tfi -> Latn
            {0x74670000, 18}, // tg -> Cyrl
            {0x7467504B,  2}, // tg-PK -> Arab
            {0x88D30000, 46}, // tgc -> Latn
            {0xB8D30000, 46}, // tgo -> Latn
            {0xD0D30000, 46}, // tgu -> Latn
            {0x74680000, 93}, // th -> Thai
            {0xACF30000, 19}, // thl -> Deva
            {0xC0F30000, 19}, // thq -> Deva
            {0xC4F30000, 19}, // thr -> Deva
            {0x74690000, 21}, // ti -> Ethi
            {0x95130000, 46}, // tif -> Latn
            {0x99130000, 21}, // tig -> Ethi
            {0xA9130000, 46}, // tik -> Latn
            {0xB1130000, 46}, // tim -> Latn
            {0xB9130000, 46}, // tio -> Latn
            {0xD5130000, 46}, // tiv -> Latn
            {0x746B0000, 46}, // tk -> Latn
            {0xAD530000, 46}, // tkl -> Latn
            {0xC5530000, 46}, // tkr -> Latn
            {0xCD530000, 19}, // tkt -> Deva
            {0x746C0000, 46}, // tl -> Latn
            {0x95730000, 46}, // tlf -> Latn
            {0xDD730000, 46}, // tlx -> Latn
            {0xE1730000, 46}, // tly -> Latn
            {0x9D930000, 46}, // tmh -> Latn
            {0xE1930000, 46}, // tmy -> Latn
            {0x746E0000, 46}, // tn -> Latn
            {0x9DB30000, 46}, // tnh -> Latn
            {0x746F0000, 46}, // to -> Latn
            {0x95D30000, 46}, // tof -> Latn
            {0x99D30000, 46}, // tog -> Latn
            {0xA9D30000, 46}, // tok -> Latn
            {0xC1D30000, 46}, // toq -> Latn
            {0xA1F30000, 46}, // tpi -> Latn
            {0xB1F30000, 46}, // tpm -> Latn
            {0xE5F30000, 46}, // tpz -> Latn
            {0xBA130000, 46}, // tqo -> Latn
            {0x74720000, 46}, // tr -> Latn
            {0xD2330000, 46}, // tru -> Latn
            {0xD6330000, 46}, // trv -> Latn
            {0xDA330000,  2}, // trw -> Arab
            {0x74730000, 46}, // ts -> Latn
            {0x8E530000, 26}, // tsd -> Grek
            {0x96530000, 19}, // tsf -> Deva
            {0x9A530000, 46}, // tsg -> Latn
            {0xA6530000, 94}, // tsj -> Tibt
            {0xDA530000, 46}, // tsw -> Latn
            {0x74740000, 18}, // tt -> Cyrl
            {0x8E730000, 46}, // ttd -> Latn
            {0x92730000, 46}, // tte -> Latn
            {0xA6730000, 46}, // ttj -> Latn
            {0xC6730000, 46}, // ttr -> Latn
            {0xCA730000, 93}, // tts -> Thai
            {0xCE730000, 46}, // ttt -> Latn
            {0x9E930000, 46}, // tuh -> Latn
            {0xAE930000, 46}, // tul -> Latn
            {0xB2930000, 46}, // tum -> Latn
            {0xC2930000, 46}, // tuq -> Latn
            {0x8EB30000, 46}, // tvd -> Latn
            {0xAEB30000, 46}, // tvl -> Latn
            {0xD2B30000, 46}, // tvu -> Latn
            {0x9ED30000, 46}, // twh -> Latn
            {0xC2D30000, 46}, // twq -> Latn
            {0x9AF30000, 88}, // txg -> Tang
            {0xBAF30000, 96}, // txo -> Toto
            {0x74790000, 46}, // ty -> Latn
            {0x83130000, 46}, // tya -> Latn
            {0xD7130000, 18}, // tyv -> Cyrl
            {0xB3330000, 46}, // tzm -> Latn
            {0xD0340000, 46}, // ubu -> Latn
            {0xA0740000,  0}, // udi -> Aghb
            {0xB0740000, 18}, // udm -> Cyrl
            {0x75670000,  2}, // ug -> Arab
            {0x75674B5A, 18}, // ug-KZ -> Cyrl
            {0x75674D4E, 18}, // ug-MN -> Cyrl
            {0x80D40000, 97}, // uga -> Ugar
            {0x756B0000, 18}, // uk -> Cyrl
            {0xA1740000, 46}, // uli -> Latn
            {0x85940000, 46}, // umb -> Latn
            {0xC5B40000,  8}, // unr -> Beng
            {0xC5B44E50, 19}, // unr-NP -> Deva
            {0xDDB40000,  8}, // unx -> Beng
            {0xA9D40000, 46}, // uok -> Latn
            {0x75720000,  2}, // ur -> Arab
            {0xA2340000, 46}, // uri -> Latn
            {0xCE340000, 46}, // urt -> Latn
            {0xDA340000, 46}, // urw -> Latn
            {0x82540000, 46}, // usa -> Latn
            {0x9E740000, 46}, // uth -> Latn
            {0xC6740000, 46}, // utr -> Latn
            {0x9EB40000, 46}, // uvh -> Latn
            {0xAEB40000, 46}, // uvl -> Latn
            {0x757A0000, 46}, // uz -> Latn
            {0x757A4146,  2}, // uz-AF -> Arab
            {0x757A434E, 18}, // uz-CN -> Cyrl
            {0x98150000, 46}, // vag -> Latn
            {0xA0150000, 98}, // vai -> Vaii
            {0xB4150000, 46}, // van -> Latn
            {0x76650000, 46}, // ve -> Latn
            {0x88950000, 46}, // vec -> Latn
            {0xBC950000, 46}, // vep -> Latn
            {0x76690000, 46}, // vi -> Latn
            {0x89150000, 46}, // vic -> Latn
            {0xD5150000, 46}, // viv -> Latn
            {0xC9750000, 46}, // vls -> Latn
            {0x95950000, 46}, // vmf -> Latn
            {0xD9950000, 46}, // vmw -> Latn
            {0x766F0000, 46}, // vo -> Latn
            {0xCDD50000, 46}, // vot -> Latn
            {0xBA350000, 46}, // vro -> Latn
            {0xB6950000, 46}, // vun -> Latn
            {0xCE950000, 46}, // vut -> Latn
            {0x77610000, 46}, // wa -> Latn
            {0x90160000, 46}, // wae -> Latn
            {0xA4160000, 46}, // waj -> Latn
            {0xAC160000, 21}, // wal -> Ethi
            {0xB4160000, 46}, // wan -> Latn
            {0xC4160000, 46}, // war -> Latn
            {0xBC360000, 46}, // wbp -> Latn
            {0xC0360000, 90}, // wbq -> Telu
            {0xC4360000, 19}, // wbr -> Deva
            {0xA0560000, 46}, // wci -> Latn
            {0xC4960000, 46}, // wer -> Latn
            {0xA0D60000, 46}, // wgi -> Latn
            {0x98F60000, 46}, // whg -> Latn
            {0x85160000, 46}, // wib -> Latn
            {0xD1160000, 46}, // wiu -> Latn
            {0xD5160000, 46}, // wiv -> Latn
            {0x81360000, 46}, // wja -> Latn
            {0xA1360000, 46}, // wji -> Latn
            {0xC9760000, 46}, // wls -> Latn
            {0xB9960000, 46}, // wmo -> Latn
            {0x89B60000, 46}, // wnc -> Latn
            {0xA1B60000,  2}, // wni -> Arab
            {0xD1B60000, 46}, // wnu -> Latn
            {0x776F0000, 46}, // wo -> Latn
            {0x85D60000, 46}, // wob -> Latn
            {0xC9D60000, 46}, // wos -> Latn
            {0xCA360000, 46}, // wrs -> Latn
            {0x9A560000, 23}, // wsg -> Gong
            {0xAA560000, 46}, // wsk -> Latn
            {0xB2760000, 19}, // wtm -> Deva
            {0xD2960000, 29}, // wuu -> Hans
            {0xD6960000, 46}, // wuv -> Latn
            {0x82D60000, 46}, // wwa -> Latn
            {0xD4170000, 46}, // xav -> Latn
            {0xA0370000, 46}, // xbi -> Latn
            {0xB8570000, 15}, // xco -> Chrs
            {0xC4570000, 12}, // xcr -> Cari
            {0xC8970000, 46}, // xes -> Latn
            {0x78680000, 46}, // xh -> Latn
            {0x81770000, 46}, // xla -> Latn
            {0x89770000, 50}, // xlc -> Lyci
            {0x8D770000, 51}, // xld -> Lydi
            {0x95970000, 22}, // xmf -> Geor
            {0xB5970000, 53}, // xmn -> Mani
            {0xC5970000, 55}, // xmr -> Merc
            {0x81B70000, 60}, // xna -> Narb
            {0xC5B70000, 19}, // xnr -> Deva
            {0x99D70000, 46}, // xog -> Latn
            {0xB5D70000, 46}, // xon -> Latn
            {0xC5F70000, 73}, // xpr -> Prti
            {0x86370000, 46}, // xrb -> Latn
            {0x82570000, 77}, // xsa -> Sarb
            {0xA2570000, 46}, // xsi -> Latn
            {0xB2570000, 46}, // xsm -> Latn
            {0xC6570000, 19}, // xsr -> Deva
            {0x92D70000, 46}, // xwe -> Latn
            {0xB0180000, 46}, // yam -> Latn
            {0xB8180000, 46}, // yao -> Latn
            {0xBC180000, 46}, // yap -> Latn
            {0xC8180000, 46}, // yas -> Latn
            {0xCC180000, 46}, // yat -> Latn
            {0xD4180000, 46}, // yav -> Latn
            {0xE0180000, 46}, // yay -> Latn
            {0xE4180000, 46}, // yaz -> Latn
            {0x80380000, 46}, // yba -> Latn
            {0x84380000, 46}, // ybb -> Latn
            {0xE0380000, 46}, // yby -> Latn
            {0xC4980000, 46}, // yer -> Latn
            {0xC4D80000, 46}, // ygr -> Latn
            {0xD8D80000, 46}, // ygw -> Latn
            {0x79690000, 31}, // yi -> Hebr
            {0xB9580000, 46}, // yko -> Latn
            {0x91780000, 46}, // yle -> Latn
            {0x99780000, 46}, // ylg -> Latn
            {0xAD780000, 46}, // yll -> Latn
            {0xAD980000, 46}, // yml -> Latn
            {0x796F0000, 46}, // yo -> Latn
            {0xB5D80000, 46}, // yon -> Latn
            {0x86380000, 46}, // yrb -> Latn
            {0x92380000, 46}, // yre -> Latn
            {0xAE380000, 46}, // yrl -> Latn
            {0xCA580000, 46}, // yss -> Latn
            {0x82980000, 46}, // yua -> Latn
            {0x92980000, 30}, // yue -> Hant
            {0x9298434E, 29}, // yue-CN -> Hans
            {0xA6980000, 46}, // yuj -> Latn
            {0xCE980000, 46}, // yut -> Latn
            {0xDA980000, 46}, // yuw -> Latn
            {0x7A610000, 46}, // za -> Latn
            {0x98190000, 46}, // zag -> Latn
            {0xA4790000,  2}, // zdj -> Arab
            {0x80990000, 46}, // zea -> Latn
            {0x9CD90000, 91}, // zgh -> Tfng
            {0x7A680000, 29}, // zh -> Hans
            {0x7A684155, 30}, // zh-AU -> Hant
            {0x7A68424E, 30}, // zh-BN -> Hant
            {0x7A684742, 30}, // zh-GB -> Hant
            {0x7A684746, 30}, // zh-GF -> Hant
            {0x7A68484B, 30}, // zh-HK -> Hant
            {0x7A684944, 30}, // zh-ID -> Hant
            {0x7A684D4F, 30}, // zh-MO -> Hant
            {0x7A685041, 30}, // zh-PA -> Hant
            {0x7A685046, 30}, // zh-PF -> Hant
            {0x7A685048, 30}, // zh-PH -> Hant
            {0x7A685352, 30}, // zh-SR -> Hant
            {0x7A685448, 30}, // zh-TH -> Hant
            {0x7A685457, 30}, // zh-TW -> Hant
            {0x7A685553, 30}, // zh-US -> Hant
            {0x7A68564E, 30}, // zh-VN -> Hant
            {0xDCF90000, 62}, // zhx -> Nshu
            {0x81190000, 46}, // zia -> Latn
            {0xCD590000, 41}, // zkt -> Kits
            {0xB1790000, 46}, // zlm -> Latn
            {0xA1990000, 46}, // zmi -> Latn
            {0x91B90000, 46}, // zne -> Latn
            {0x7A750000, 46}, // zu -> Latn
            {0x83390000, 46}, // zza -> Latn
    };


    public static final long[] REPRESENTATIVE_LOCALES = new long[]{
            0x616145544C61746EL, // aa_Latn_ET
            0x616247454379726CL, // ab_Cyrl_GE
            0xC42047484C61746EL, // abr_Latn_GH
            0x904049444C61746EL, // ace_Latn_ID
            0x9C4055474C61746EL, // ach_Latn_UG
            0x806047484C61746EL, // ada_Latn_GH
            0xBC60425454696274L, // adp_Tibt_BT
            0xE06052554379726CL, // ady_Cyrl_RU
            0x6165495241767374L, // ae_Avst_IR
            0x8480544E41726162L, // aeb_Arab_TN
            0x61665A414C61746EL, // af_Latn_ZA
            0xC0C0434D4C61746EL, // agq_Latn_CM
            0xB8E0494E41686F6DL, // aho_Ahom_IN
            0xCD20544E41726162L, // ajt_Arab_TN
            0x616B47484C61746EL, // ak_Latn_GH
            0xA940495158737578L, // akk_Xsux_IQ
            0xB560584B4C61746EL, // aln_Latn_XK
            0xCD6052554379726CL, // alt_Cyrl_RU
            0x616D455445746869L, // am_Ethi_ET
            0xB9804E474C61746EL, // amo_Latn_NG
            0x616E45534C61746EL, // an_Latn_ES
            0xB5A04E474C61746EL, // ann_Latn_NG
            0xE5C049444C61746EL, // aoz_Latn_ID
            0x8DE0544741726162L, // apd_Arab_TG
            0x6172454741726162L, // ar_Arab_EG
            0x8A20495241726D69L, // arc_Armi_IR
            0x8A204A4F4E626174L, // arc_Nbat_JO
            0x8A20535950616C6DL, // arc_Palm_SY
            0xB620434C4C61746EL, // arn_Latn_CL
            0xBA20424F4C61746EL, // aro_Latn_BO
            0xC220445A41726162L, // arq_Arab_DZ
            0xCA20534141726162L, // ars_Arab_SA
            0xE2204D4141726162L, // ary_Arab_MA
            0xE620454741726162L, // arz_Arab_EG
            0x6173494E42656E67L, // as_Beng_IN
            0x8240545A4C61746EL, // asa_Latn_TZ
            0x9240555353676E77L, // ase_Sgnw_US
            0xCE4045534C61746EL, // ast_Latn_ES
            0xA66043414C61746EL, // atj_Latn_CA
            0x617652554379726CL, // av_Cyrl_RU
            0x82C0494E44657661L, // awa_Deva_IN
            0x6179424F4C61746EL, // ay_Latn_BO
            0x617A495241726162L, // az_Arab_IR
            0x617A415A4C61746EL, // az_Latn_AZ
            0x626152554379726CL, // ba_Cyrl_RU
            0xAC01504B41726162L, // bal_Arab_PK
            0xB40149444C61746EL, // ban_Latn_ID
            0xBC014E5044657661L, // bap_Deva_NP
            0xC40141544C61746EL, // bar_Latn_AT
            0xC801434D4C61746EL, // bas_Latn_CM
            0xDC01434D42616D75L, // bax_Bamu_CM
            0x882149444C61746EL, // bbc_Latn_ID
            0xA421434D4C61746EL, // bbj_Latn_CM
            0xA04143494C61746EL, // bci_Latn_CI
            0x626542594379726CL, // be_Cyrl_BY
            0xA481534441726162L, // bej_Arab_SD
            0xB0815A4D4C61746EL, // bem_Latn_ZM
            0xD88149444C61746EL, // bew_Latn_ID
            0xE481545A4C61746EL, // bez_Latn_TZ
            0x8CA1434D4C61746EL, // bfd_Latn_CM
            0xC0A1494E54616D6CL, // bfq_Taml_IN
            0xCCA1504B41726162L, // bft_Arab_PK
            0xE0A1494E44657661L, // bfy_Deva_IN
            0x626742474379726CL, // bg_Cyrl_BG
            0x88C1494E44657661L, // bgc_Deva_IN
            0xB4C1504B41726162L, // bgn_Arab_PK
            0xDCC154524772656BL, // bgx_Grek_TR
            0x84E1494E44657661L, // bhb_Deva_IN
            0xA0E1494E44657661L, // bhi_Deva_IN
            0xB8E1494E44657661L, // bho_Deva_IN
            0x626956554C61746EL, // bi_Latn_VU
            0xA90150484C61746EL, // bik_Latn_PH
            0xB5014E474C61746EL, // bin_Latn_NG
            0xA521494E44657661L, // bjj_Deva_IN
            0xB52149444C61746EL, // bjn_Latn_ID
            0xCD21534E4C61746EL, // bjt_Latn_SN
            0xB141434D4C61746EL, // bkm_Latn_CM
            0xD14150484C61746EL, // bku_Latn_PH
            0x816143414C61746EL, // bla_Latn_CA
            0x99614D594C61746EL, // blg_Latn_MY
            0xCD61564E54617674L, // blt_Tavt_VN
            0x626D4D4C4C61746EL, // bm_Latn_ML
            0xC1814D4C4C61746EL, // bmq_Latn_ML
            0x626E424442656E67L, // bn_Beng_BD
            0x626F434E54696274L, // bo_Tibt_CN
            0xE1E1494E42656E67L, // bpy_Beng_IN
            0xA201495241726162L, // bqi_Arab_IR
            0xD60143494C61746EL, // bqv_Latn_CI
            0x627246524C61746EL, // br_Latn_FR
            0x8221494E44657661L, // bra_Deva_IN
            0x9E21504B41726162L, // brh_Arab_PK
            0xDE21494E44657661L, // brx_Deva_IN
            0x627342414C61746EL, // bs_Latn_BA
            0xC2414C5242617373L, // bsq_Bass_LR
            0xCA41434D4C61746EL, // bss_Latn_CM
            0xBA6150484C61746EL, // bto_Latn_PH
            0xD661504B44657661L, // btv_Deva_PK
            0x828152554379726CL, // bua_Cyrl_RU
            0x8A8159544C61746EL, // buc_Latn_YT
            0x9A8149444C61746EL, // bug_Latn_ID
            0xB281434D4C61746EL, // bum_Latn_CM
            0x86A147514C61746EL, // bvb_Latn_GQ
            0xB701455245746869L, // byn_Ethi_ER
            0xD701434D4C61746EL, // byv_Latn_CM
            0x93214D4C4C61746EL, // bze_Latn_ML
            0x636145534C61746EL, // ca_Latn_ES
            0x8C0255534C61746EL, // cad_Latn_US
            0x9C424E474C61746EL, // cch_Latn_NG
            0xBC42424443616B6DL, // ccp_Cakm_BD
            0x636552554379726CL, // ce_Cyrl_RU
            0x848250484C61746EL, // ceb_Latn_PH
            0x98C255474C61746EL, // cgg_Latn_UG
            0x636847554C61746EL, // ch_Latn_GU
            0xA8E2464D4C61746EL, // chk_Latn_FM
            0xB0E252554379726CL, // chm_Cyrl_RU
            0xB8E255534C61746EL, // cho_Latn_US
            0xBCE243414C61746EL, // chp_Latn_CA
            0xC4E2555343686572L, // chr_Cher_US
            0x890255534C61746EL, // cic_Latn_US
            0x81224B4841726162L, // cja_Arab_KH
            0xB122564E4368616DL, // cjm_Cham_VN
            0x8542495141726162L, // ckb_Arab_IQ
            0x896243414C61746EL, // clc_Latn_CA
            0x99824D4E536F796FL, // cmg_Soyo_MN
            0x636F46524C61746EL, // co_Latn_FR
            0xBDC24547436F7074L, // cop_Copt_EG
            0xC9E250484C61746EL, // cps_Latn_PH
            0x6372434143616E73L, // cr_Cans_CA
            0x9A2243414C61746EL, // crg_Latn_CA
            0x9E2255414379726CL, // crh_Cyrl_UA
            0xAA22434143616E73L, // crk_Cans_CA
            0xAE22434143616E73L, // crl_Cans_CA
            0xCA2253434C61746EL, // crs_Latn_SC
            0x6373435A4C61746EL, // cs_Latn_CZ
            0x8642504C4C61746EL, // csb_Latn_PL
            0xDA42434143616E73L, // csw_Cans_CA
            0x8E624D4D50617563L, // ctd_Pauc_MM
            0x637552554379726CL, // cu_Cyrl_RU
            0x63754247476C6167L, // cu_Glag_BG
            0x637652554379726CL, // cv_Cyrl_RU
            0x637947424C61746EL, // cy_Latn_GB
            0x6461444B4C61746EL, // da_Latn_DK
            0x940343494C61746EL, // daf_Latn_CI
            0xA80355534C61746EL, // dak_Latn_US
            0xC40352554379726CL, // dar_Cyrl_RU
            0xD4034B454C61746EL, // dav_Latn_KE
            0x8843494E41726162L, // dcc_Arab_IN
            0x646544454C61746EL, // de_Latn_DE
            0xB48343414C61746EL, // den_Latn_CA
            0xC4C343414C61746EL, // dgr_Latn_CA
            0x91234E454C61746EL, // dje_Latn_NE
            0x95834E474D656466L, // dmf_Medf_NG
            0xA5A343494C61746EL, // dnj_Latn_CI
            0xA1C3494E44657661L, // doi_Deva_IN
            0x9E23434E4D6F6E67L, // drh_Mong_CN
            0x864344454C61746EL, // dsb_Latn_DE
            0xB2634D4C4C61746EL, // dtm_Latn_ML
            0xBE634D594C61746EL, // dtp_Latn_MY
            0xE2634E5044657661L, // dty_Deva_NP
            0x8283434D4C61746EL, // dua_Latn_CM
            0x64764D5654686161L, // dv_Thaa_MV
            0xBB03534E4C61746EL, // dyo_Latn_SN
            0xD30342464C61746EL, // dyu_Latn_BF
            0x647A425454696274L, // dz_Tibt_BT
            0xD0244B454C61746EL, // ebu_Latn_KE
            0x656547484C61746EL, // ee_Latn_GH
            0xA0A44E474C61746EL, // efi_Latn_NG
            0xACC449544C61746EL, // egl_Latn_IT
            0xE0C4454745677970L, // egy_Egyp_EG
            0xE1444D4D4B616C69L, // eky_Kali_MM
            0x656C47524772656BL, // el_Grek_GR
            0x656E47424C61746EL, // en_Latn_GB
            0x656E55534C61746EL, // en_Latn_US
            0x656E474253686177L, // en_Shaw_GB
            0x657345534C61746EL, // es_Latn_ES
            0x65734D584C61746EL, // es_Latn_MX
            0x657355534C61746EL, // es_Latn_US
            0x9A44494E476F6E6DL, // esg_Gonm_IN
            0xD24455534C61746EL, // esu_Latn_US
            0x657445454C61746EL, // et_Latn_EE
            0xCE6449544974616CL, // ett_Ital_IT
            0x657545534C61746EL, // eu_Latn_ES
            0xBAC4434D4C61746EL, // ewo_Latn_CM
            0xCEE445534C61746EL, // ext_Latn_ES
            0x6661495241726162L, // fa_Arab_IR
            0xB40547514C61746EL, // fan_Latn_GQ
            0x6666474E41646C6DL, // ff_Adlm_GN
            0x6666534E4C61746EL, // ff_Latn_SN
            0xB0A54D4C4C61746EL, // ffm_Latn_ML
            0x666946494C61746EL, // fi_Latn_FI
            0x8105534441726162L, // fia_Arab_SD
            0xAD0550484C61746EL, // fil_Latn_PH
            0xCD0553454C61746EL, // fit_Latn_SE
            0x666A464A4C61746EL, // fj_Latn_FJ
            0x666F464F4C61746EL, // fo_Latn_FO
            0xB5C5424A4C61746EL, // fon_Latn_BJ
            0x667246524C61746EL, // fr_Latn_FR
            0x8A2555534C61746EL, // frc_Latn_US
            0xBE2546524C61746EL, // frp_Latn_FR
            0xC62544454C61746EL, // frr_Latn_DE
            0xCA2544454C61746EL, // frs_Latn_DE
            0x8685434D41726162L, // fub_Arab_CM
            0x8E8557464C61746EL, // fud_Latn_WF
            0x9685474E4C61746EL, // fuf_Latn_GN
            0xC2854E454C61746EL, // fuq_Latn_NE
            0xC68549544C61746EL, // fur_Latn_IT
            0xD6854E474C61746EL, // fuv_Latn_NG
            0xC6A553444C61746EL, // fvr_Latn_SD
            0x66794E4C4C61746EL, // fy_Latn_NL
            0x676149454C61746EL, // ga_Latn_IE
            0x800647484C61746EL, // gaa_Latn_GH
            0x98064D444C61746EL, // gag_Latn_MD
            0xB406434E48616E73L, // gan_Hans_CN
            0xE00649444C61746EL, // gay_Latn_ID
            0xB026494E44657661L, // gbm_Deva_IN
            0xE426495241726162L, // gbz_Arab_IR
            0xC44647464C61746EL, // gcr_Latn_GF
            0x676447424C61746EL, // gd_Latn_GB
            0xE486455445746869L, // gez_Ethi_ET
            0xB4C64E5044657661L, // ggn_Deva_NP
            0xAD064B494C61746EL, // gil_Latn_KI
            0xA926504B41726162L, // gjk_Arab_PK
            0xD126504B41726162L, // gju_Arab_PK
            0x676C45534C61746EL, // gl_Latn_ES
            0xA966495241726162L, // glk_Arab_IR
            0x676E50594C61746EL, // gn_Latn_PY
            0xB1C6494E44657661L, // gom_Deva_IN
            0xB5C6494E54656C75L, // gon_Telu_IN
            0xC5C649444C61746EL, // gor_Latn_ID
            0xC9C64E4C4C61746EL, // gos_Latn_NL
            0xCDC65541476F7468L, // got_Goth_UA
            0x8A26435943707274L, // grc_Cprt_CY
            0x8A2647524C696E62L, // grc_Linb_GR
            0xCE26494E42656E67L, // grt_Beng_IN
            0xDA4643484C61746EL, // gsw_Latn_CH
            0x6775494E47756A72L, // gu_Gujr_IN
            0x868642524C61746EL, // gub_Latn_BR
            0x8A86434F4C61746EL, // guc_Latn_CO
            0xC68647484C61746EL, // gur_Latn_GH
            0xE6864B454C61746EL, // guz_Latn_KE
            0x6776494D4C61746EL, // gv_Latn_IM
            0xC6A64E5044657661L, // gvr_Deva_NP
            0xA2C643414C61746EL, // gwi_Latn_CA
            0x68614E474C61746EL, // ha_Latn_NG
            0xA807434E48616E73L, // hak_Hans_CN
            0xD80755534C61746EL, // haw_Latn_US
            0xE407414641726162L, // haz_Arab_AF
            0x6865494C48656272L, // he_Hebr_IL
            0x6869494E44657661L, // hi_Deva_IN
            0x6869494E4C61746EL, // hi_Latn_IN
            0x9507464A4C61746EL, // hif_Latn_FJ
            0xAD0750484C61746EL, // hil_Latn_PH
            0xD1675452486C7577L, // hlu_Hluw_TR
            0x8D87434E506C7264L, // hmd_Plrd_CN
            0x8DA7504B41726162L, // hnd_Arab_PK
            0x91A7494E44657661L, // hne_Deva_IN
            0xA5A75553486D6E70L, // hnj_Hmnp_US
            0xB5A750484C61746EL, // hnn_Latn_PH
            0xB9A7504B41726162L, // hno_Arab_PK
            0x686F50474C61746EL, // ho_Latn_PG
            0x89C7494E44657661L, // hoc_Deva_IN
            0xA5C7494E44657661L, // hoj_Deva_IN
            0x687248524C61746EL, // hr_Latn_HR
            0x864744454C61746EL, // hsb_Latn_DE
            0xB647434E48616E73L, // hsn_Hans_CN
            0x687448544C61746EL, // ht_Latn_HT
            0x687548554C61746EL, // hu_Latn_HU
            0xC68743414C61746EL, // hur_Latn_CA
            0x6879414D41726D6EL, // hy_Armn_AM
            0x687A4E414C61746EL, // hz_Latn_NA
            0x80284D594C61746EL, // iba_Latn_MY
            0x84284E474C61746EL, // ibb_Latn_NG
            0x696449444C61746EL, // id_Latn_ID
            0x90A854474C61746EL, // ife_Latn_TG
            0x69674E474C61746EL, // ig_Latn_NG
            0x6969434E59696969L, // ii_Yiii_CN
            0x696B55534C61746EL, // ik_Latn_US
            0xB96850484C61746EL, // ilo_Latn_PH
            0x696E49444C61746EL, // in_Latn_ID
            0x9DA852554379726CL, // inh_Cyrl_RU
            0x697349534C61746EL, // is_Latn_IS
            0x697449544C61746EL, // it_Latn_IT
            0x6975434143616E73L, // iu_Cans_CA
            0x6977494C48656272L, // iw_Hebr_IL
            0x9F2852554C61746EL, // izh_Latn_RU
            0x6A614A504A70616EL, // ja_Jpan_JP
            0xB0094A4D4C61746EL, // jam_Latn_JM
            0xB8C9434D4C61746EL, // jgo_Latn_CM
            0x8989545A4C61746EL, // jmc_Latn_TZ
            0xAD894E5044657661L, // jml_Deva_NP
            0xCE89444B4C61746EL, // jut_Latn_DK
            0x6A7649444C61746EL, // jv_Latn_ID
            0x6A7749444C61746EL, // jw_Latn_ID
            0x6B61474547656F72L, // ka_Geor_GE
            0x800A555A4379726CL, // kaa_Cyrl_UZ
            0x840A445A4C61746EL, // kab_Latn_DZ
            0x880A4D4D4C61746EL, // kac_Latn_MM
            0xA40A4E474C61746EL, // kaj_Latn_NG
            0xB00A4B454C61746EL, // kam_Latn_KE
            0xB80A4D4C4C61746EL, // kao_Latn_ML
            0xD80A49444B617769L, // kaw_Kawi_ID
            0x8C2A52554379726CL, // kbd_Cyrl_RU
            0xE02A4E4541726162L, // kby_Arab_NE
            0x984A4E474C61746EL, // kcg_Latn_NG
            0xA84A5A574C61746EL, // kck_Latn_ZW
            0x906A545A4C61746EL, // kde_Latn_TZ
            0x9C6A54474C61746EL, // kdh_Latn_TG
            0xCC6A544854686169L, // kdt_Thai_TH
            0x808A43564C61746EL, // kea_Latn_CV
            0xB48A434D4C61746EL, // ken_Latn_CM
            0xB8AA43494C61746EL, // kfo_Latn_CI
            0xC4AA494E44657661L, // kfr_Deva_IN
            0xE0AA494E44657661L, // kfy_Deva_IN
            0x6B6743444C61746EL, // kg_Latn_CD
            0x90CA49444C61746EL, // kge_Latn_ID
            0xBCCA42524C61746EL, // kgp_Latn_BR
            0x80EA494E4C61746EL, // kha_Latn_IN
            0x84EA434E54616C75L, // khb_Talu_CN
            0xB4EA494E44657661L, // khn_Deva_IN
            0xC0EA4D4C4C61746EL, // khq_Latn_ML
            0xCCEA494E4D796D72L, // kht_Mymr_IN
            0xD8EA504B41726162L, // khw_Arab_PK
            0x6B694B454C61746EL, // ki_Latn_KE
            0xD10A54524C61746EL, // kiu_Latn_TR
            0x6B6A4E414C61746EL, // kj_Latn_NA
            0x992A4C414C616F6FL, // kjg_Laoo_LA
            0x6B6B434E41726162L, // kk_Arab_CN
            0x6B6B4B5A4379726CL, // kk_Cyrl_KZ
            0xA54A434D4C61746EL, // kkj_Latn_CM
            0x6B6C474C4C61746EL, // kl_Latn_GL
            0xB56A4B454C61746EL, // kln_Latn_KE
            0x6B6D4B484B686D72L, // km_Khmr_KH
            0x858A414F4C61746EL, // kmb_Latn_AO
            0x6B6E494E4B6E6461L, // kn_Knda_IN
            0x95AA47574C61746EL, // knf_Latn_GW
            0x6B6F4B524B6F7265L, // ko_Kore_KR
            0xA1CA52554379726CL, // koi_Cyrl_RU
            0xA9CA494E44657661L, // kok_Deva_IN
            0xC9CA464D4C61746EL, // kos_Latn_FM
            0x91EA4C524C61746EL, // kpe_Latn_LR
            0x8A2A52554379726CL, // krc_Cyrl_RU
            0xA22A534C4C61746EL, // kri_Latn_SL
            0xA62A50484C61746EL, // krj_Latn_PH
            0xAE2A52554C61746EL, // krl_Latn_RU
            0xD22A494E44657661L, // kru_Deva_IN
            0x6B73494E41726162L, // ks_Arab_IN
            0x864A545A4C61746EL, // ksb_Latn_TZ
            0x964A434D4C61746EL, // ksf_Latn_CM
            0x9E4A44454C61746EL, // ksh_Latn_DE
            0xC66A4D594C61746EL, // ktr_Latn_MY
            0x6B75495141726162L, // ku_Arab_IQ
            0x6B7554524C61746EL, // ku_Latn_TR
            0x6B75474559657A69L, // ku_Yezi_GE
            0xB28A52554379726CL, // kum_Cyrl_RU
            0x6B7652554379726CL, // kv_Cyrl_RU
            0xC6AA49444C61746EL, // kvr_Latn_ID
            0xDEAA504B41726162L, // kvx_Arab_PK
            0x6B7747424C61746EL, // kw_Latn_GB
            0xAACA43414C61746EL, // kwk_Latn_CA
            0xAEEA494E44657661L, // kxl_Deva_IN
            0xB2EA544854686169L, // kxm_Thai_TH
            0xBEEA504B41726162L, // kxp_Arab_PK
            0x6B79434E41726162L, // ky_Arab_CN
            0x6B794B474379726CL, // ky_Cyrl_KG
            0x6B7954524C61746EL, // ky_Latn_TR
            0xA72A4D594C61746EL, // kzj_Latn_MY
            0xCF2A4D594C61746EL, // kzt_Latn_MY
            0x6C6156414C61746EL, // la_Latn_VA
            0x840B47524C696E61L, // lab_Lina_GR
            0x8C0B494C48656272L, // lad_Hebr_IL
            0x980B545A4C61746EL, // lag_Latn_TZ
            0x9C0B504B41726162L, // lah_Arab_PK
            0xA40B55474C61746EL, // laj_Latn_UG
            0x6C624C554C61746EL, // lb_Latn_LU
            0x902B52554379726CL, // lbe_Cyrl_RU
            0xD82B49444C61746EL, // lbw_Latn_ID
            0xBC4B434E54686169L, // lcp_Thai_CN
            0xBC8B494E4C657063L, // lep_Lepc_IN
            0xE48B52554379726CL, // lez_Cyrl_RU
            0x6C6755474C61746EL, // lg_Latn_UG
            0x6C694E4C4C61746EL, // li_Latn_NL
            0x950B4E5044657661L, // lif_Deva_NP
            0x950B494E4C696D62L, // lif_Limb_IN
            0xA50B49544C61746EL, // lij_Latn_IT
            0xAD0B43414C61746EL, // lil_Latn_CA
            0xC90B434E4C697375L, // lis_Lisu_CN
            0xBD2B49444C61746EL, // ljp_Latn_ID
            0xA14B495241726162L, // lki_Arab_IR
            0xCD4B55534C61746EL, // lkt_Latn_US
            0xB58B494E54656C75L, // lmn_Telu_IN
            0xB98B49544C61746EL, // lmo_Latn_IT
            0x6C6E43444C61746EL, // ln_Latn_CD
            0x6C6F4C414C616F6FL, // lo_Laoo_LA
            0xADCB43444C61746EL, // lol_Latn_CD
            0xE5CB5A4D4C61746EL, // loz_Latn_ZM
            0x8A2B495241726162L, // lrc_Arab_IR
            0x6C744C544C61746EL, // lt_Latn_LT
            0x9A6B4C564C61746EL, // ltg_Latn_LV
            0x6C7543444C61746EL, // lu_Latn_CD
            0x828B43444C61746EL, // lua_Latn_CD
            0xBA8B4B454C61746EL, // luo_Latn_KE
            0xE28B4B454C61746EL, // luy_Latn_KE
            0xE68B495241726162L, // luz_Arab_IR
            0x6C764C564C61746EL, // lv_Latn_LV
            0xAECB544854686169L, // lwl_Thai_TH
            0x9F2B434E48616E73L, // lzh_Hans_CN
            0xE72B54524C61746EL, // lzz_Latn_TR
            0x8C0C49444C61746EL, // mad_Latn_ID
            0x940C434D4C61746EL, // maf_Latn_CM
            0x980C494E44657661L, // mag_Deva_IN
            0xA00C494E44657661L, // mai_Deva_IN
            0xA80C49444C61746EL, // mak_Latn_ID
            0xB40C474D4C61746EL, // man_Latn_GM
            0xB40C474E4E6B6F6FL, // man_Nkoo_GN
            0xC80C4B454C61746EL, // mas_Latn_KE
            0xE40C4D584C61746EL, // maz_Latn_MX
            0x946C52554379726CL, // mdf_Cyrl_RU
            0x9C6C50484C61746EL, // mdh_Latn_PH
            0xC46C49444C61746EL, // mdr_Latn_ID
            0xB48C534C4C61746EL, // men_Latn_SL
            0xC48C4B454C61746EL, // mer_Latn_KE
            0x80AC544841726162L, // mfa_Arab_TH
            0x90AC4D554C61746EL, // mfe_Latn_MU
            0x6D674D474C61746EL, // mg_Latn_MG
            0x9CCC4D5A4C61746EL, // mgh_Latn_MZ
            0xB8CC434D4C61746EL, // mgo_Latn_CM
            0xBCCC4E5044657661L, // mgp_Deva_NP
            0xE0CC545A4C61746EL, // mgy_Latn_TZ
            0x6D684D484C61746EL, // mh_Latn_MH
            0x6D694E5A4C61746EL, // mi_Latn_NZ
            0x890C43414C61746EL, // mic_Latn_CA
            0xB50C49444C61746EL, // min_Latn_ID
            0x6D6B4D4B4379726CL, // mk_Cyrl_MK
            0x6D6C494E4D6C796DL, // ml_Mlym_IN
            0xC96C53444C61746EL, // mls_Latn_SD
            0x6D6E4D4E4379726CL, // mn_Cyrl_MN
            0x6D6E434E4D6F6E67L, // mn_Mong_CN
            0xA1AC494E42656E67L, // mni_Beng_IN
            0xD9AC4D4D4D796D72L, // mnw_Mymr_MM
            0x6D6F524F4C61746EL, // mo_Latn_RO
            0x91CC43414C61746EL, // moe_Latn_CA
            0x9DCC43414C61746EL, // moh_Latn_CA
            0xC9CC42464C61746EL, // mos_Latn_BF
            0x6D72494E44657661L, // mr_Deva_IN
            0x8E2C4E5044657661L, // mrd_Deva_NP
            0xA62C52554379726CL, // mrj_Cyrl_RU
            0xBA2C42444D726F6FL, // mro_Mroo_BD
            0x6D734D594C61746EL, // ms_Latn_MY
            0x6D744D544C61746EL, // mt_Latn_MT
            0xC66C494E44657661L, // mtr_Deva_IN
            0x828C434D4C61746EL, // mua_Latn_CM
            0xCA8C55534C61746EL, // mus_Latn_US
            0xE2AC504B41726162L, // mvy_Arab_PK
            0xAACC4D4C4C61746EL, // mwk_Latn_ML
            0xC6CC494E44657661L, // mwr_Deva_IN
            0xD6CC49444C61746EL, // mwv_Latn_ID
            0xDACC5553486D6E70L, // mww_Hmnp_US
            0x8AEC5A574C61746EL, // mxc_Latn_ZW
            0x6D794D4D4D796D72L, // my_Mymr_MM
            0xD70C52554379726CL, // myv_Cyrl_RU
            0xDF0C55474C61746EL, // myx_Latn_UG
            0xE70C49524D616E64L, // myz_Mand_IR
            0xB72C495241726162L, // mzn_Arab_IR
            0x6E614E524C61746EL, // na_Latn_NR
            0xB40D434E48616E73L, // nan_Hans_CN
            0xBC0D49544C61746EL, // nap_Latn_IT
            0xC00D4E414C61746EL, // naq_Latn_NA
            0x6E624E4F4C61746EL, // nb_Latn_NO
            0x9C4D4D584C61746EL, // nch_Latn_MX
            0x6E645A574C61746EL, // nd_Latn_ZW
            0x886D4D5A4C61746EL, // ndc_Latn_MZ
            0xC86D44454C61746EL, // nds_Latn_DE
            0x6E654E5044657661L, // ne_Deva_NP
            0xD88D4E5044657661L, // new_Deva_NP
            0x6E674E414C61746EL, // ng_Latn_NA
            0xACCD4D5A4C61746EL, // ngl_Latn_MZ
            0x90ED4D584C61746EL, // nhe_Latn_MX
            0xD8ED4D584C61746EL, // nhw_Latn_MX
            0xA50D49444C61746EL, // nij_Latn_ID
            0xD10D4E554C61746EL, // niu_Latn_NU
            0xB92D494E4C61746EL, // njo_Latn_IN
            0x6E6C4E4C4C61746EL, // nl_Latn_NL
            0x998D434D4C61746EL, // nmg_Latn_CM
            0x6E6E4E4F4C61746EL, // nn_Latn_NO
            0x9DAD434D4C61746EL, // nnh_Latn_CM
            0xBDAD494E5763686FL, // nnp_Wcho_IN
            0x6E6F4E4F4C61746EL, // no_Latn_NO
            0x8DCD54484C616E61L, // nod_Lana_TH
            0x91CD494E44657661L, // noe_Deva_IN
            0xB5CD534552756E72L, // non_Runr_SE
            0xBA0D474E4E6B6F6FL, // nqo_Nkoo_GN
            0x6E725A414C61746EL, // nr_Latn_ZA
            0xAA4D434143616E73L, // nsk_Cans_CA
            0xBA4D5A414C61746EL, // nso_Latn_ZA
            0xCE4D494E546E7361L, // nst_Tnsa_IN
            0xCA8D53534C61746EL, // nus_Latn_SS
            0x6E7655534C61746EL, // nv_Latn_US
            0xC2ED434E4C61746EL, // nxq_Latn_CN
            0x6E794D574C61746EL, // ny_Latn_MW
            0xB30D545A4C61746EL, // nym_Latn_TZ
            0xB70D55474C61746EL, // nyn_Latn_UG
            0xA32D47484C61746EL, // nzi_Latn_GH
            0x6F6346524C61746EL, // oc_Latn_FR
            0x6F6A434143616E73L, // oj_Cans_CA
            0xC92E434143616E73L, // ojs_Cans_CA
            0x814E43414C61746EL, // oka_Latn_CA
            0x6F6D45544C61746EL, // om_Latn_ET
            0x6F72494E4F727961L, // or_Orya_IN
            0x6F7347454379726CL, // os_Cyrl_GE
            0x824E55534F736765L, // osa_Osge_US
            0xAA6E4D4E4F726B68L, // otk_Orkh_MN
            0xA28E8C814F756772L, // oui_Ougr_143
            0x7061504B41726162L, // pa_Arab_PK
            0x7061494E47757275L, // pa_Guru_IN
            0x980F50484C61746EL, // pag_Latn_PH
            0xAC0F495250686C69L, // pal_Phli_IR
            0xAC0F434E50686C70L, // pal_Phlp_CN
            0xB00F50484C61746EL, // pam_Latn_PH
            0xBC0F41574C61746EL, // pap_Latn_AW
            0xD00F50574C61746EL, // pau_Latn_PW
            0x8C4F46524C61746EL, // pcd_Latn_FR
            0xB04F4E474C61746EL, // pcm_Latn_NG
            0x886F55534C61746EL, // pdc_Latn_US
            0xCC6F43414C61746EL, // pdt_Latn_CA
            0xB88F49525870656FL, // peo_Xpeo_IR
            0xACAF44454C61746EL, // pfl_Latn_DE
            0xB4EF4C4250686E78L, // phn_Phnx_LB
            0xC90F53424C61746EL, // pis_Latn_SB
            0x814F494E42726168L, // pka_Brah_IN
            0xB94F4B454C61746EL, // pko_Latn_KE
            0x706C504C4C61746EL, // pl_Latn_PL
            0xC98F49544C61746EL, // pms_Latn_IT
            0xCDAF47524772656BL, // pnt_Grek_GR
            0xB5CF464D4C61746EL, // pon_Latn_FM
            0x81EF494E44657661L, // ppa_Deva_IN
            0xB20F43414C61746EL, // pqm_Latn_CA
            0x822F504B4B686172L, // pra_Khar_PK
            0x8E2F495241726162L, // prd_Arab_IR
            0x7073414641726162L, // ps_Arab_AF
            0x707442524C61746EL, // pt_Latn_BR
            0xD28F47414C61746EL, // puu_Latn_GA
            0x717550454C61746EL, // qu_Latn_PE
            0x8A9047544C61746EL, // quc_Latn_GT
            0x9A9045434C61746EL, // qug_Latn_EC
            0xA411494E44657661L, // raj_Deva_IN
            0x945152454C61746EL, // rcf_Latn_RE
            0xA49149444C61746EL, // rej_Latn_ID
            0xB4D149544C61746EL, // rgn_Latn_IT
            0x98F14D4D526F6867L, // rhg_Rohg_MM
            0x8111494E4C61746EL, // ria_Latn_IN
            0x95114D4154666E67L, // rif_Tfng_MA
            0xC9314E5044657661L, // rjs_Deva_NP
            0xCD51424442656E67L, // rkt_Beng_BD
            0x726D43484C61746EL, // rm_Latn_CH
            0x959146494C61746EL, // rmf_Latn_FI
            0xB99143484C61746EL, // rmo_Latn_CH
            0xCD91495241726162L, // rmt_Arab_IR
            0xD19153454C61746EL, // rmu_Latn_SE
            0x726E42494C61746EL, // rn_Latn_BI
            0x99B14D5A4C61746EL, // rng_Latn_MZ
            0x726F524F4C61746EL, // ro_Latn_RO
            0x85D149444C61746EL, // rob_Latn_ID
            0x95D1545A4C61746EL, // rof_Latn_TZ
            0xB271464A4C61746EL, // rtm_Latn_FJ
            0x727552554379726CL, // ru_Cyrl_RU
            0x929155414379726CL, // rue_Cyrl_UA
            0x9A9153424C61746EL, // rug_Latn_SB
            0x727752574C61746EL, // rw_Latn_RW
            0xAAD1545A4C61746EL, // rwk_Latn_TZ
            0xD3114A504B616E61L, // ryu_Kana_JP
            0x7361494E44657661L, // sa_Deva_IN
            0x941247484C61746EL, // saf_Latn_GH
            0x9C1252554379726CL, // sah_Cyrl_RU
            0xC0124B454C61746EL, // saq_Latn_KE
            0xC81249444C61746EL, // sas_Latn_ID
            0xCC12494E4F6C636BL, // sat_Olck_IN
            0xD412534E4C61746EL, // sav_Latn_SN
            0xE412494E53617572L, // saz_Saur_IN
            0xBC32545A4C61746EL, // sbp_Latn_TZ
            0x736349544C61746EL, // sc_Latn_IT
            0xA852494E44657661L, // sck_Deva_IN
            0xB45249544C61746EL, // scn_Latn_IT
            0xB85247424C61746EL, // sco_Latn_GB
            0x7364504B41726162L, // sd_Arab_PK
            0x7364494E44657661L, // sd_Deva_IN
            0x7364494E4B686F6AL, // sd_Khoj_IN
            0x7364494E53696E64L, // sd_Sind_IN
            0x887249544C61746EL, // sdc_Latn_IT
            0x9C72495241726162L, // sdh_Arab_IR
            0x73654E4F4C61746EL, // se_Latn_NO
            0x949243494C61746EL, // sef_Latn_CI
            0x9C924D5A4C61746EL, // seh_Latn_MZ
            0xA0924D584C61746EL, // sei_Latn_MX
            0xC8924D4C4C61746EL, // ses_Latn_ML
            0x736743464C61746EL, // sg_Latn_CF
            0x80D249454F67616DL, // sga_Ogam_IE
            0xC8D24C544C61746EL, // sgs_Latn_LT
            0xA0F24D4154666E67L, // shi_Tfng_MA
            0xB4F24D4D4D796D72L, // shn_Mymr_MM
            0x73694C4B53696E68L, // si_Sinh_LK
            0x8D1245544C61746EL, // sid_Latn_ET
            0x736B534B4C61746EL, // sk_Latn_SK
            0xC552504B41726162L, // skr_Arab_PK
            0x736C53494C61746EL, // sl_Latn_SI
            0xA172504C4C61746EL, // sli_Latn_PL
            0xE17249444C61746EL, // sly_Latn_ID
            0x736D57534C61746EL, // sm_Latn_WS
            0x819253454C61746EL, // sma_Latn_SE
            0x8D92414F4C61746EL, // smd_Latn_AO
            0xA59253454C61746EL, // smj_Latn_SE
            0xB59246494C61746EL, // smn_Latn_FI
            0xBD92494C53616D72L, // smp_Samr_IL
            0xC99246494C61746EL, // sms_Latn_FI
            0x736E5A574C61746EL, // sn_Latn_ZW
            0x85B24D594C61746EL, // snb_Latn_MY
            0xA9B24D4C4C61746EL, // snk_Latn_ML
            0x736F534F4C61746EL, // so_Latn_SO
            0x99D2555A536F6764L, // sog_Sogd_UZ
            0xD1D2544854686169L, // sou_Thai_TH
            0x7371414C4C61746EL, // sq_Latn_AL
            0x737252534379726CL, // sr_Cyrl_RS
            0x737252534C61746EL, // sr_Latn_RS
            0x8632494E536F7261L, // srb_Sora_IN
            0xB63253524C61746EL, // srn_Latn_SR
            0xC632534E4C61746EL, // srr_Latn_SN
            0xDE32494E44657661L, // srx_Deva_IN
            0x73735A414C61746EL, // ss_Latn_ZA
            0xE25245524C61746EL, // ssy_Latn_ER
            0x73745A414C61746EL, // st_Latn_ZA
            0xC27244454C61746EL, // stq_Latn_DE
            0x737549444C61746EL, // su_Latn_ID
            0xAA92545A4C61746EL, // suk_Latn_TZ
            0xCA92474E4C61746EL, // sus_Latn_GN
            0x737653454C61746EL, // sv_Latn_SE
            0x7377545A4C61746EL, // sw_Latn_TZ
            0x86D2595441726162L, // swb_Arab_YT
            0x8AD243444C61746EL, // swc_Latn_CD
            0x9AD244454C61746EL, // swg_Latn_DE
            0xD6D2494E44657661L, // swv_Deva_IN
            0xB6F249444C61746EL, // sxn_Latn_ID
            0xAF12424442656E67L, // syl_Beng_BD
            0xC712495153797263L, // syr_Syrc_IQ
            0xAF32504C4C61746EL, // szl_Latn_PL
            0x7461494E54616D6CL, // ta_Taml_IN
            0xA4134E5044657661L, // taj_Deva_NP
            0xD83350484C61746EL, // tbw_Latn_PH
            0xE053494E4B6E6461L, // tcy_Knda_IN
            0x8C73434E54616C65L, // tdd_Tale_CN
            0x98734E5044657661L, // tdg_Deva_NP
            0x9C734E5044657661L, // tdh_Deva_NP
            0xD0734D594C61746EL, // tdu_Latn_MY
            0x7465494E54656C75L, // te_Telu_IN
            0xB093534C4C61746EL, // tem_Latn_SL
            0xB89355474C61746EL, // teo_Latn_UG
            0xCC93544C4C61746EL, // tet_Latn_TL
            0x7467504B41726162L, // tg_Arab_PK
            0x7467544A4379726CL, // tg_Cyrl_TJ
            0x7468544854686169L, // th_Thai_TH
            0xACF34E5044657661L, // thl_Deva_NP
            0xC0F34E5044657661L, // thq_Deva_NP
            0xC4F34E5044657661L, // thr_Deva_NP
            0x7469455445746869L, // ti_Ethi_ET
            0x9913455245746869L, // tig_Ethi_ER
            0xD5134E474C61746EL, // tiv_Latn_NG
            0x746B544D4C61746EL, // tk_Latn_TM
            0xAD53544B4C61746EL, // tkl_Latn_TK
            0xC553415A4C61746EL, // tkr_Latn_AZ
            0xCD534E5044657661L, // tkt_Deva_NP
            0x746C50484C61746EL, // tl_Latn_PH
            0xE173415A4C61746EL, // tly_Latn_AZ
            0x9D934E454C61746EL, // tmh_Latn_NE
            0x746E5A414C61746EL, // tn_Latn_ZA
            0x746F544F4C61746EL, // to_Latn_TO
            0x99D34D574C61746EL, // tog_Latn_MW
            0xA1F350474C61746EL, // tpi_Latn_PG
            0x747254524C61746EL, // tr_Latn_TR
            0xD23354524C61746EL, // tru_Latn_TR
            0xD63354574C61746EL, // trv_Latn_TW
            0xDA33504B41726162L, // trw_Arab_PK
            0x74735A414C61746EL, // ts_Latn_ZA
            0x8E5347524772656BL, // tsd_Grek_GR
            0x96534E5044657661L, // tsf_Deva_NP
            0x9A5350484C61746EL, // tsg_Latn_PH
            0xA653425454696274L, // tsj_Tibt_BT
            0x747452554379726CL, // tt_Cyrl_RU
            0xA67355474C61746EL, // ttj_Latn_UG
            0xCA73544854686169L, // tts_Thai_TH
            0xCE73415A4C61746EL, // ttt_Latn_AZ
            0xB2934D574C61746EL, // tum_Latn_MW
            0xAEB354564C61746EL, // tvl_Latn_TV
            0xC2D34E454C61746EL, // twq_Latn_NE
            0x9AF3434E54616E67L, // txg_Tang_CN
            0xBAF3494E546F746FL, // txo_Toto_IN
            0x747950464C61746EL, // ty_Latn_PF
            0xD71352554379726CL, // tyv_Cyrl_RU
            0xB3334D414C61746EL, // tzm_Latn_MA
            0xA074525541676862L, // udi_Aghb_RU
            0xB07452554379726CL, // udm_Cyrl_RU
            0x7567434E41726162L, // ug_Arab_CN
            0x75674B5A4379726CL, // ug_Cyrl_KZ
            0x80D4535955676172L, // uga_Ugar_SY
            0x756B55414379726CL, // uk_Cyrl_UA
            0xA174464D4C61746EL, // uli_Latn_FM
            0x8594414F4C61746EL, // umb_Latn_AO
            0xC5B4494E42656E67L, // unr_Beng_IN
            0xC5B44E5044657661L, // unr_Deva_NP
            0xDDB4494E42656E67L, // unx_Beng_IN
            0x7572504B41726162L, // ur_Arab_PK
            0x757A414641726162L, // uz_Arab_AF
            0x757A555A4C61746EL, // uz_Latn_UZ
            0xA0154C5256616969L, // vai_Vaii_LR
            0x76655A414C61746EL, // ve_Latn_ZA
            0x889549544C61746EL, // vec_Latn_IT
            0xBC9552554C61746EL, // vep_Latn_RU
            0x7669564E4C61746EL, // vi_Latn_VN
            0x891553584C61746EL, // vic_Latn_SX
            0xC97542454C61746EL, // vls_Latn_BE
            0x959544454C61746EL, // vmf_Latn_DE
            0xD9954D5A4C61746EL, // vmw_Latn_MZ
            0xCDD552554C61746EL, // vot_Latn_RU
            0xBA3545454C61746EL, // vro_Latn_EE
            0xB695545A4C61746EL, // vun_Latn_TZ
            0x776142454C61746EL, // wa_Latn_BE
            0x901643484C61746EL, // wae_Latn_CH
            0xAC16455445746869L, // wal_Ethi_ET
            0xC41650484C61746EL, // war_Latn_PH
            0xBC3641554C61746EL, // wbp_Latn_AU
            0xC036494E54656C75L, // wbq_Telu_IN
            0xC436494E44657661L, // wbr_Deva_IN
            0xC97657464C61746EL, // wls_Latn_WF
            0xA1B64B4D41726162L, // wni_Arab_KM
            0x776F534E4C61746EL, // wo_Latn_SN
            0x9A56494E476F6E67L, // wsg_Gong_IN
            0xB276494E44657661L, // wtm_Deva_IN
            0xD296434E48616E73L, // wuu_Hans_CN
            0xD41742524C61746EL, // xav_Latn_BR
            0xB857555A43687273L, // xco_Chrs_UZ
            0xC457545243617269L, // xcr_Cari_TR
            0x78685A414C61746EL, // xh_Latn_ZA
            0x897754524C796369L, // xlc_Lyci_TR
            0x8D7754524C796469L, // xld_Lydi_TR
            0x9597474547656F72L, // xmf_Geor_GE
            0xB597434E4D616E69L, // xmn_Mani_CN
            0xC59753444D657263L, // xmr_Merc_SD
            0x81B753414E617262L, // xna_Narb_SA
            0xC5B7494E44657661L, // xnr_Deva_IN
            0x99D755474C61746EL, // xog_Latn_UG
            0xC5F7495250727469L, // xpr_Prti_IR
            0x8257594553617262L, // xsa_Sarb_YE
            0xC6574E5044657661L, // xsr_Deva_NP
            0xB8184D5A4C61746EL, // yao_Latn_MZ
            0xBC18464D4C61746EL, // yap_Latn_FM
            0xD418434D4C61746EL, // yav_Latn_CM
            0x8438434D4C61746EL, // ybb_Latn_CM
            0x796F4E474C61746EL, // yo_Latn_NG
            0xAE3842524C61746EL, // yrl_Latn_BR
            0x82984D584C61746EL, // yua_Latn_MX
            0x9298434E48616E73L, // yue_Hans_CN
            0x9298484B48616E74L, // yue_Hant_HK
            0x7A61434E4C61746EL, // za_Latn_CN
            0x981953444C61746EL, // zag_Latn_SD
            0xA4794B4D41726162L, // zdj_Arab_KM
            0x80994E4C4C61746EL, // zea_Latn_NL
            0x9CD94D4154666E67L, // zgh_Tfng_MA
            0x7A685457426F706FL, // zh_Bopo_TW
            0x7A68545748616E62L, // zh_Hanb_TW
            0x7A68434E48616E73L, // zh_Hans_CN
            0x7A68545748616E74L, // zh_Hant_TW
            0xDCF9434E4E736875L, // zhx_Nshu_CN
            0xCD59434E4B697473L, // zkt_Kits_CN
            0xB17954474C61746EL, // zlm_Latn_TG
            0xA1994D594C61746EL, // zmi_Latn_MY
            0x7A755A414C61746EL, // zu_Latn_ZA
            0x833954524C61746EL // zza_Latn_TR
    };


    public static final int[][] ARAB_PARENTS = new int[][]{
            {0x61724145, 0x61729420}, // ar-AE -> ar-015
            {0x6172445A, 0x61729420}, // ar-DZ -> ar-015
            {0x61724548, 0x61729420}, // ar-EH -> ar-015
            {0x61724C59, 0x61729420}, // ar-LY -> ar-015
            {0x61724D41, 0x61729420}, // ar-MA -> ar-015
            {0x6172544E, 0x61729420}, // ar-TN -> ar-015
    };

    public static final int[][] DEVA_PARENTS = new int[][]{
            {0x68690000, 0x656E494E}, // hi-Latn -> en-IN
    };

    public static final int[][] HANT_PARENTS = new int[][]{
            {0x7A684D4F, 0x7A68484B}, // zh-Hant-MO -> zh-Hant-HK
    };


    static final int[][] LATN_PARENTS = new int[][]{
            {0x656E80A1, 0x656E8400}, // en-150 -> en-001
            {0x656E4147, 0x656E8400}, // en-AG -> en-001
            {0x656E4149, 0x656E8400}, // en-AI -> en-001
            {0x656E4154, 0x656E80A1}, // en-AT -> en-150
            {0x656E4155, 0x656E8400}, // en-AU -> en-001
            {0x656E4242, 0x656E8400}, // en-BB -> en-001
            {0x656E4245, 0x656E80A1}, // en-BE -> en-150
            {0x656E424D, 0x656E8400}, // en-BM -> en-001
            {0x656E4253, 0x656E8400}, // en-BS -> en-001
            {0x656E4257, 0x656E8400}, // en-BW -> en-001
            {0x656E425A, 0x656E8400}, // en-BZ -> en-001
            {0x656E4343, 0x656E8400}, // en-CC -> en-001
            {0x656E4348, 0x656E80A1}, // en-CH -> en-150
            {0x656E434B, 0x656E8400}, // en-CK -> en-001
            {0x656E434D, 0x656E8400}, // en-CM -> en-001
            {0x656E4358, 0x656E8400}, // en-CX -> en-001
            {0x656E4359, 0x656E8400}, // en-CY -> en-001
            {0x656E4445, 0x656E80A1}, // en-DE -> en-150
            {0x656E4447, 0x656E8400}, // en-DG -> en-001
            {0x656E444B, 0x656E80A1}, // en-DK -> en-150
            {0x656E444D, 0x656E8400}, // en-DM -> en-001
            {0x656E4552, 0x656E8400}, // en-ER -> en-001
            {0x656E4649, 0x656E80A1}, // en-FI -> en-150
            {0x656E464A, 0x656E8400}, // en-FJ -> en-001
            {0x656E464B, 0x656E8400}, // en-FK -> en-001
            {0x656E464D, 0x656E8400}, // en-FM -> en-001
            {0x656E4742, 0x656E8400}, // en-GB -> en-001
            {0x656E4744, 0x656E8400}, // en-GD -> en-001
            {0x656E4747, 0x656E8400}, // en-GG -> en-001
            {0x656E4748, 0x656E8400}, // en-GH -> en-001
            {0x656E4749, 0x656E8400}, // en-GI -> en-001
            {0x656E474D, 0x656E8400}, // en-GM -> en-001
            {0x656E4759, 0x656E8400}, // en-GY -> en-001
            {0x656E484B, 0x656E8400}, // en-HK -> en-001
            {0x656E4945, 0x656E8400}, // en-IE -> en-001
            {0x656E494C, 0x656E8400}, // en-IL -> en-001
            {0x656E494D, 0x656E8400}, // en-IM -> en-001
            {0x656E494E, 0x656E8400}, // en-IN -> en-001
            {0x656E494F, 0x656E8400}, // en-IO -> en-001
            {0x656E4A45, 0x656E8400}, // en-JE -> en-001
            {0x656E4A4D, 0x656E8400}, // en-JM -> en-001
            {0x656E4B45, 0x656E8400}, // en-KE -> en-001
            {0x656E4B49, 0x656E8400}, // en-KI -> en-001
            {0x656E4B4E, 0x656E8400}, // en-KN -> en-001
            {0x656E4B59, 0x656E8400}, // en-KY -> en-001
            {0x656E4C43, 0x656E8400}, // en-LC -> en-001
            {0x656E4C52, 0x656E8400}, // en-LR -> en-001
            {0x656E4C53, 0x656E8400}, // en-LS -> en-001
            {0x656E4D47, 0x656E8400}, // en-MG -> en-001
            {0x656E4D4F, 0x656E8400}, // en-MO -> en-001
            {0x656E4D53, 0x656E8400}, // en-MS -> en-001
            {0x656E4D54, 0x656E8400}, // en-MT -> en-001
            {0x656E4D55, 0x656E8400}, // en-MU -> en-001
            {0x656E4D56, 0x656E8400}, // en-MV -> en-001
            {0x656E4D57, 0x656E8400}, // en-MW -> en-001
            {0x656E4D59, 0x656E8400}, // en-MY -> en-001
            {0x656E4E41, 0x656E8400}, // en-NA -> en-001
            {0x656E4E46, 0x656E8400}, // en-NF -> en-001
            {0x656E4E47, 0x656E8400}, // en-NG -> en-001
            {0x656E4E4C, 0x656E80A1}, // en-NL -> en-150
            {0x656E4E52, 0x656E8400}, // en-NR -> en-001
            {0x656E4E55, 0x656E8400}, // en-NU -> en-001
            {0x656E4E5A, 0x656E8400}, // en-NZ -> en-001
            {0x656E5047, 0x656E8400}, // en-PG -> en-001
            {0x656E504B, 0x656E8400}, // en-PK -> en-001
            {0x656E504E, 0x656E8400}, // en-PN -> en-001
            {0x656E5057, 0x656E8400}, // en-PW -> en-001
            {0x656E5257, 0x656E8400}, // en-RW -> en-001
            {0x656E5342, 0x656E8400}, // en-SB -> en-001
            {0x656E5343, 0x656E8400}, // en-SC -> en-001
            {0x656E5344, 0x656E8400}, // en-SD -> en-001
            {0x656E5345, 0x656E80A1}, // en-SE -> en-150
            {0x656E5347, 0x656E8400}, // en-SG -> en-001
            {0x656E5348, 0x656E8400}, // en-SH -> en-001
            {0x656E5349, 0x656E80A1}, // en-SI -> en-150
            {0x656E534C, 0x656E8400}, // en-SL -> en-001
            {0x656E5353, 0x656E8400}, // en-SS -> en-001
            {0x656E5358, 0x656E8400}, // en-SX -> en-001
            {0x656E535A, 0x656E8400}, // en-SZ -> en-001
            {0x656E5443, 0x656E8400}, // en-TC -> en-001
            {0x656E544B, 0x656E8400}, // en-TK -> en-001
            {0x656E544F, 0x656E8400}, // en-TO -> en-001
            {0x656E5454, 0x656E8400}, // en-TT -> en-001
            {0x656E5456, 0x656E8400}, // en-TV -> en-001
            {0x656E545A, 0x656E8400}, // en-TZ -> en-001
            {0x656E5547, 0x656E8400}, // en-UG -> en-001
            {0x656E5643, 0x656E8400}, // en-VC -> en-001
            {0x656E5647, 0x656E8400}, // en-VG -> en-001
            {0x656E5655, 0x656E8400}, // en-VU -> en-001
            {0x656E5753, 0x656E8400}, // en-WS -> en-001
            {0x656E5A41, 0x656E8400}, // en-ZA -> en-001
            {0x656E5A4D, 0x656E8400}, // en-ZM -> en-001
            {0x656E5A57, 0x656E8400}, // en-ZW -> en-001
            {0x65734152, 0x6573A424}, // es-AR -> es-419
            {0x6573424F, 0x6573A424}, // es-BO -> es-419
            {0x65734252, 0x6573A424}, // es-BR -> es-419
            {0x6573425A, 0x6573A424}, // es-BZ -> es-419
            {0x6573434C, 0x6573A424}, // es-CL -> es-419
            {0x6573434F, 0x6573A424}, // es-CO -> es-419
            {0x65734352, 0x6573A424}, // es-CR -> es-419
            {0x65734355, 0x6573A424}, // es-CU -> es-419
            {0x6573444F, 0x6573A424}, // es-DO -> es-419
            {0x65734543, 0x6573A424}, // es-EC -> es-419
            {0x65734754, 0x6573A424}, // es-GT -> es-419
            {0x6573484E, 0x6573A424}, // es-HN -> es-419
            {0x65734D58, 0x6573A424}, // es-MX -> es-419
            {0x65734E49, 0x6573A424}, // es-NI -> es-419
            {0x65735041, 0x6573A424}, // es-PA -> es-419
            {0x65735045, 0x6573A424}, // es-PE -> es-419
            {0x65735052, 0x6573A424}, // es-PR -> es-419
            {0x65735059, 0x6573A424}, // es-PY -> es-419
            {0x65735356, 0x6573A424}, // es-SV -> es-419
            {0x65735553, 0x6573A424}, // es-US -> es-419
            {0x65735559, 0x6573A424}, // es-UY -> es-419
            {0x65735645, 0x6573A424}, // es-VE -> es-419
            {0x6E620000, 0x6E6F0000}, // nb -> no
            {0x6E6E0000, 0x6E6F0000}, // nn -> no
            {0x7074414F, 0x70745054}, // pt-AO -> pt-PT
            {0x70744348, 0x70745054}, // pt-CH -> pt-PT
            {0x70744356, 0x70745054}, // pt-CV -> pt-PT
            {0x70744751, 0x70745054}, // pt-GQ -> pt-PT
            {0x70744757, 0x70745054}, // pt-GW -> pt-PT
            {0x70744C55, 0x70745054}, // pt-LU -> pt-PT
            {0x70744D4F, 0x70745054}, // pt-MO -> pt-PT
            {0x70744D5A, 0x70745054}, // pt-MZ -> pt-PT
            {0x70745354, 0x70745054}, // pt-ST -> pt-PT
            {0x7074544C, 0x70745054}, // pt-TL -> pt-PT
    };


    public static final int[][] ___B_PARENTS = new int[][]{
        {0x61725842, 0x61729420}, // ar-XB -> ar-015
    };

    public static final ScriptParent[] SCRIPT_PARENTS = new ScriptParent[]{
           new ScriptParent(new char[]{'A', 'r', 'a', 'b'}, ARAB_PARENTS),
            new ScriptParent(new char[]{'D', 'e', 'v', 'a'}, DEVA_PARENTS),
            new ScriptParent(new char[]{'H', 'a', 'n', 't'}, HANT_PARENTS),
            new ScriptParent(new char[]{'L', 'a', 't', 'n'}, LATN_PARENTS),
            new ScriptParent(new char[]{'~', '~', '~', 'B'}, ___B_PARENTS),
    };

    public static final int MAX_PARENT_DEPTH = 3;

    public static class ScriptParent {
        public final char[] script;
        public final int[][] map;
        public ScriptParent(char[] script, int[][] map){
            this.script = script;
            this.map = map;
        }
    }
}
