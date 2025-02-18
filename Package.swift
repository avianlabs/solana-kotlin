// swift-tools-version:5.9
import PackageDescription

let package = Package(
    name: "SolanaKotlin",
    platforms: [
        .iOS(.v16)
    ],
    products: [
        .library(
            name: "SolanaKotlin",
            targets: ["SolanaKotlin"]
        ),
    ],
    targets: [
        .binaryTarget(
            name: "SolanaKotlin",
            url: "https://github.com/avianlabs/solana-kotlin/releases/download/0.3.3/SolanaKotlin.zip",
            checksum: "e9f6672c2cccc011d5b86135de68b7de36cc61686195e00441e740a73a2d62b0"
        ),
    ]
)
