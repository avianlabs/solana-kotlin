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
            url: "https://github.com/avianlabs/solana-kotlin/releases/download/0.1.6/SolanaKotlin.zip",
            checksum: "2a3f448b29613eabdbd0bb9727454252e47348db04071ba82cb30bd5ed7e5be7"
        ),
    ]
)
